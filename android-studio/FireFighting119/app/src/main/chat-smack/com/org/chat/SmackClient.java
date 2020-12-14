package com.org.chat;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.org.chat.data.entity.JsonExtraBodyItem;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.chat2.OutgoingChatMessageListener;
import org.jivesoftware.smack.debugger.ConsoleDebugger;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.packet.StanzaError;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.EntityFullJid;
import org.jxmpp.jid.Jid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class SmackClient {

    public enum State {
        IDLE("idle"),
        CONNECTED("connected"),
        CONNECT_FAILED("connect failed"),
        ACCOUNT_CREATED("account created"),
        ACCOUNT_CREATE_FAILED("account create failed"),
        AUTHENTICATED("authenticated"),
        AUTHENTICATE_FAILED("authenticate failed"),
        CONNECTION_CLOSED_ERROR("connection closed error"),
        CONNECTION_CLOSED_ERROR_CONFLICT("connection closed error conflict");

        private final String message;

        State(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public interface StateChangeListener {
        void onStateChange(State state);
    }

    private State mState = State.IDLE;

    private final CopyOnWriteArrayList<StateChangeListener> mStateChangeListeners = new CopyOnWriteArrayList<>();

    public void addStateChangeListener(StateChangeListener stateChangeListener) {
        mStateChangeListeners.add(stateChangeListener);
    }

    public void removeStateChangeListener(StateChangeListener stateChangeListener) {
        mStateChangeListeners.remove(stateChangeListener);
    }

    private void notifySateChange(State state) {
        mState = state;
        for (StateChangeListener stateChangeListener : mStateChangeListeners) {
            stateChangeListener.onStateChange(mState);
        }
        Timber.d(state.getMessage());
    }

    public interface MessageListener {
        void newOutgoingMessage(Message message);

        void newIncomingMessage(Message message);

        void newOfflineMessage(Message message);

        void onReceiptReceived(Jid fromJid, Jid toJid, String receiptId, Stanza receipt);
    }

    private final CopyOnWriteArrayList<MessageListener> mMessageListeners = new CopyOnWriteArrayList<>();

    public void addMessageListener(MessageListener l) {
        mMessageListeners.add(l);
    }

    public void removeMessageListener(MessageListener l) {
        mMessageListeners.remove(l);
    }

    private final ExecutorService mActionThreadPool;

    public static String HOST = "47.108.88.62";

    private final static int PORT = 5222;

    public static String DOMAIN = "ff-chat";

    private String mUsername;

    private String mPassword;

    private XMPPTCPConnection mXMPPTCPConnection;

    private final OutgoingChatMessageListener mOutgoingChatMessageListener = (from, message, chat) -> {
        for (MessageListener l : mMessageListeners) {
            l.newOutgoingMessage(message);
        }
    };
    private final IncomingChatMessageListener mIncomingChatMessageListener = (to, message, chat) -> {
        for (MessageListener l : mMessageListeners) {
            l.newIncomingMessage(message);
        }
    };
    private final ReceiptReceivedListener mReceiptReceivedListener = (fromJid, toJid, receiptId, receipt) -> {
        for (MessageListener l : mMessageListeners) {
            l.onReceiptReceived(fromJid, toJid, receiptId, receipt);
        }
    };

    private final ConnectionListener mConnectionListener = new ConnectionListener() {

        @Override
        public void connected(XMPPConnection connection) {

        }

        @Override
        public void authenticated(XMPPConnection connection, boolean resumed) {
            handleOfflineMessage();
            Presence presence = new Presence(Presence.Type.available);
            try {
                mXMPPTCPConnection.sendStanza(presence);
            } catch (SmackException.NotConnectedException | InterruptedException e) {
                Timber.e(e);
            }
        }

        @Override
        public void connectionClosed() {
            mActionThreadPool.execute(() -> notifySateChange(State.IDLE));
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            mActionThreadPool.execute(() -> {
                notifySateChange(State.CONNECTION_CLOSED_ERROR);
                if (!TextUtils.isEmpty(e.getMessage()) && e.getMessage().contains("conflict")) {
                    notifySateChange(State.CONNECTION_CLOSED_ERROR_CONFLICT);
                    Timber.w("你的账号已经在别处登录，暂时不能接收聊天消息");
                    stop();
                }
            });
        }
    };

    SmackClient() {
        ThreadPoolExecutor temp = new ThreadPoolExecutor(1, 1,
                30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        temp.allowCoreThreadTimeOut(true);
        this.mActionThreadPool = temp;
    }

    private boolean connect() {
        try {
            mXMPPTCPConnection = new XMPPTCPConnection(
                    XMPPTCPConnectionConfiguration.builder()
                            .setHostAddressByNameOrIp(HOST)
                            .setPort(PORT)
                            .setXmppDomain(JidCreate.domainBareFromOrThrowUnchecked(DOMAIN))
                            .setResource(Resourcepart.fromOrThrowUnchecked("SmackAndroidClient"))
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                            .setSendPresence(false)
                            .setDebuggerFactory(ConsoleDebugger.Factory.INSTANCE)
                            .build());
            mXMPPTCPConnection.addConnectionListener(mConnectionListener);

            DeliveryReceiptManager.getInstanceFor(mXMPPTCPConnection).setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
            DeliveryReceiptManager.getInstanceFor(mXMPPTCPConnection).autoAddDeliveryReceiptRequests(); // normal chat headline
            DeliveryReceiptManager.getInstanceFor(mXMPPTCPConnection).addReceiptReceivedListener(mReceiptReceivedListener);

            ReconnectionManager.getInstanceFor(mXMPPTCPConnection).enableAutomaticReconnection();

            mXMPPTCPConnection.connect();
            ChatManager.getInstanceFor(mXMPPTCPConnection).addOutgoingListener(mOutgoingChatMessageListener);
            ChatManager.getInstanceFor(mXMPPTCPConnection).addIncomingListener(mIncomingChatMessageListener);
            notifySateChange(State.CONNECTED);
            return true;
        } catch (InterruptedException | SmackException | IOException | XMPPException e) {
            Timber.e(e);
            notifySateChange(State.CONNECT_FAILED);
            return false;
        }
    }

    private boolean createAccount() {
        try {
            AccountManager accountManager = AccountManager.getInstance(mXMPPTCPConnection);
            accountManager.sensitiveOperationOverInsecureConnection(true);
            accountManager.createAccount(Localpart.from(mUsername), mPassword);
            notifySateChange(State.ACCOUNT_CREATED);
            return true;
        } catch (SmackException.NoResponseException
                | XmppStringprepException
                | XMPPException.XMPPErrorException
                | InterruptedException
                | SmackException.NotConnectedException e) {
            if (e instanceof XMPPException.XMPPErrorException
                    && ((XMPPException.XMPPErrorException) e).getStanzaError().getCondition() == StanzaError.Condition.conflict) {
                notifySateChange(State.ACCOUNT_CREATED);
                Timber.w(e);
                return true;
            } else {
                Timber.e(e);
                notifySateChange(State.ACCOUNT_CREATE_FAILED);
                return false;
            }
        }
    }

    private boolean login() {
        try {
            mXMPPTCPConnection.login(mUsername, mPassword);
            notifySateChange(State.AUTHENTICATED);
            return true;
        } catch (XMPPException
                | SmackException
                | InterruptedException
                | IOException e) {
            Timber.e(e);
            notifySateChange(State.AUTHENTICATE_FAILED);
            return false;
        }
    }

    private void start() {
        if (mState != SmackClient.State.AUTHENTICATED) {
            if (connect()) {
                if (createAccount()) {
                    login();
                }
            }
        } else {
            Timber.w("already authenticated, please stop first");
        }
    }

    private void stop() {
        if (mState != SmackClient.State.IDLE) {
            ChatManager.getInstanceFor(mXMPPTCPConnection).removeOutgoingListener(mOutgoingChatMessageListener);
            ChatManager.getInstanceFor(mXMPPTCPConnection).removeIncomingListener(mIncomingChatMessageListener);

            ReconnectionManager.getInstanceFor(mXMPPTCPConnection).abortPossiblyRunningReconnection();
            ReconnectionManager.getInstanceFor(mXMPPTCPConnection).disableAutomaticReconnection();

            DeliveryReceiptManager.getInstanceFor(mXMPPTCPConnection).setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.disabled);
            DeliveryReceiptManager.getInstanceFor(mXMPPTCPConnection).dontAutoAddDeliveryReceiptRequests();
            DeliveryReceiptManager.getInstanceFor(mXMPPTCPConnection).removeReceiptReceivedListener(mReceiptReceivedListener);
            mXMPPTCPConnection.removeConnectionListener(mConnectionListener);
            mXMPPTCPConnection.disconnect();
            mXMPPTCPConnection = null;
            notifySateChange(State.IDLE);
        } else {
            Timber.w("already stopped");
        }
    }

    void startAsync(String username, String password) {
        mActionThreadPool.execute(() -> {
            if (TextUtils.equals(mUsername, username)
                    && mState == State.AUTHENTICATED) {
                Timber.w("already authenticated, need not to start");
            } else {
                this.mUsername = username;
                this.mPassword = password;
                stop();
                start();
            }
        });
    }

    void restartAsync() {
        mActionThreadPool.execute(() -> {
            if (mState == State.AUTHENTICATED) {
                Timber.w("already authenticated, need not to restart");
            } else {
                if (TextUtils.isEmpty(mUsername)) {
                    Timber.w("have not started yet, call startAsync() firstly");
                } else {
                    stop();
                    start();
                }
            }
        });
    }

    void stopAsync() {
        mActionThreadPool.execute(this::stop);
    }

    void destroy() {
        mActionThreadPool.shutdown();
    }

    private void handleOfflineMessage() {
        OfflineMessageManager omm = new OfflineMessageManager(mXMPPTCPConnection);
        try {
            omm.getMessageCount();
            List<Message> messages = omm.getMessages();
            for (Message m : messages) {
                for (MessageListener l : mMessageListeners) {
                    l.newOfflineMessage(m);
                }
            }
            omm.deleteMessages();
        } catch (SmackException.NoResponseException
                | XMPPException.XMPPErrorException
                | SmackException.NotConnectedException
                | InterruptedException e) {
            Timber.e(e);
        }
    }

    public State getState() {
        return mState;
    }

    public String getUsername() {
        return mUsername;
    }

    public EntityFullJid getUser() {
        if (mXMPPTCPConnection != null && mXMPPTCPConnection.isAuthenticated()) {
            return mXMPPTCPConnection.getUser();
        } else {
            return JidCreate.entityFullFromUnescapedOrThrowUnchecked(mUsername + "@" + DOMAIN + "/" + "SmackAndroidClient");
        }
    }

    /**
     * 耗时操作，建议另开线程
     */
    public boolean saveVCard(VCard vCard) {
        VCardManager vCardManager = VCardManager.getInstanceFor(mXMPPTCPConnection);
        try {
            vCardManager.saveVCard(vCard);
            return true;
        } catch (SmackException.NoResponseException
                | XMPPException.XMPPErrorException
                | SmackException.NotConnectedException
                | InterruptedException e) {
            Timber.w(e);
            return false;
        }
    }

    /**
     * 耗时操作，建议另开线程
     */
    public VCard getVCard(EntityBareJid jid) {
        VCardManager vCardManager = VCardManager.getInstanceFor(mXMPPTCPConnection);
        try {
            return vCardManager.loadVCard(jid);
        } catch (SmackException.NoResponseException
                | XMPPException.XMPPErrorException
                | SmackException.NotConnectedException
                | InterruptedException
                | IllegalArgumentException e) {
            Timber.w(e);
            return null;
        }
    }

    public void sendMessage(String jid, String body) {
        try {
            ChatManager.getInstanceFor(mXMPPTCPConnection)
                    .chatWith(JidCreate.entityBareFrom(jid)).send(body);
        } catch (XmppStringprepException | InterruptedException | SmackException.NotConnectedException e) {
            Timber.w(e);
        }
    }

    public void sendMessage(String jid, JsonExtraBodyItem body) {
        try {
            Message stanza = new Message();
            stanza.addBody("json_extra_body_item_1", new Gson().toJson(body));
            stanza.setType(Message.Type.chat);
            ChatManager.getInstanceFor(mXMPPTCPConnection)
                    .chatWith(JidCreate.entityBareFrom(jid)).send(stanza);
        } catch (XmppStringprepException | InterruptedException | SmackException.NotConnectedException e) {
            Timber.w(e);
        }
    }
}
