package com.org.chat;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.debugger.ConsoleDebugger;
import org.jivesoftware.smack.packet.StanzaError;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.jid.parts.Localpart;
import org.jxmpp.jid.parts.Resourcepart;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;

import timber.log.Timber;

public class SmackClient {

    private final static String HOST = "114.215.140.123";

    private final static int PORT = 5222;

    private final static String DOMAIN = "linestoken";

    private String mUsername;

    private String mPassword;

    private XMPPTCPConnection mXMPPTCPConnection;

    public SmackClient(String username, String password) {
        this.mUsername = username;
        this.mPassword = password;
    }

    private boolean connect() {
        try {
            mXMPPTCPConnection = new XMPPTCPConnection(
                    XMPPTCPConnectionConfiguration.builder()
                            .setHost(HOST)
                            .setPort(PORT)
                            .setXmppDomain(JidCreate.domainBareFromOrThrowUnchecked(DOMAIN))
                            .setResource(Resourcepart.fromOrThrowUnchecked("SmackAndroidClient"))
                            .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                            .setDebuggerFactory(ConsoleDebugger.Factory.INSTANCE)
                            .build());
            ProviderManager.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
            ProviderManager.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceiptRequest.Provider());
            DeliveryReceiptManager.getInstanceFor(mXMPPTCPConnection).setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.ifIsSubscribed);

            ReconnectionManager.getInstanceFor(mXMPPTCPConnection).enableAutomaticReconnection();

            mXMPPTCPConnection.connect();
            ChatManager.getInstanceFor(mXMPPTCPConnection)
                    .addIncomingListener((from, message, chat) -> {

                    });
            ChatManager.getInstanceFor(mXMPPTCPConnection)
                    .addOutgoingListener((to, messageBuilder, chat) -> {

                    });
            return true;
        } catch (InterruptedException | SmackException | IOException | XMPPException e) {
            Timber.e(e);
            return false;
        }
    }

    private boolean createAccount() {
        try {
            AccountManager accountManager = AccountManager.getInstance(mXMPPTCPConnection);
            accountManager.sensitiveOperationOverInsecureConnection(true);
            accountManager.createAccount(Localpart.from(mUsername), mPassword);
            return true;
        } catch (SmackException.NoResponseException
                | XmppStringprepException
                | XMPPException.XMPPErrorException
                | InterruptedException
                | SmackException.NotConnectedException e) {
            Timber.e(e);
            return e instanceof XMPPException.XMPPErrorException
                    && ((XMPPException.XMPPErrorException) e).getStanzaError().getCondition() == StanzaError.Condition.conflict;
        }
    }

    private boolean login() {
        try {
            mXMPPTCPConnection.login(mUsername, mPassword);
            return true;
        } catch (XMPPException
                | SmackException
                | InterruptedException
                | IOException e) {
            Timber.e(e);
            return false;
        }
    }

    public boolean start() {
        if (!connect()) {
            return false;
        }
        if (!createAccount()) {
            return false;
        }
        return login();
    }

    public void stop() {
        mXMPPTCPConnection.disconnect();
    }

    public String getUsername() {
        return mUsername;
    }

//    public void sendMessage(String message) {
//        try {
//            ChatManager.getInstanceFor(mXMPPTCPConnection).createChat(JidCreate.entityBareFrom("admin@" + DOMAIN)).sendMessage(message);
//        } catch (XmppStringprepException | InterruptedException | SmackException.NotConnectedException e) {
//            e.printStackTrace();
//        }
//    }
}
