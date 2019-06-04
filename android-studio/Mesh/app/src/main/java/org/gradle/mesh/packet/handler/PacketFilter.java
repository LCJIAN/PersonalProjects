package org.gradle.mesh.packet.handler;

import org.gradle.mesh.packet.Packet;

import java.util.LinkedList;

public abstract class PacketFilter {

    public abstract boolean accept(Packet packet);

    /**
     * Returns the logical AND of this and the specified filter.
     *
     * @param filter The filter to AND this filter with.
     * @return A filter where calling <code>accept()</code> returns the result
     * of <code>(this.accept() &amp;&amp; filter.accept())</code>.
     */
    public PacketFilter and(PacketFilter filter) {
        if (filter == null) {
            return this;
        }

        return new PacketFilterAnd(this, filter);
    }

    /**
     * Returns the logical OR of this and the specified filter.
     *
     * @param filter The filter to OR this filter with.
     * @return A filter where calling <code>accept()</code> returns the result
     * of <code>(this.accept() || filter.accept())</code>.
     */
    public PacketFilter or(PacketFilter filter) {
        if (filter == null) {
            return this;
        }

        return new PacketFilterOr(this, filter);
    }

    private static class PacketFilterAnd extends PacketFilter {
        private final LinkedList<PacketFilter> mFilters = new LinkedList<>();

        PacketFilterAnd(PacketFilter lhs, PacketFilter rhs) {
            mFilters.add(lhs);
            mFilters.add(rhs);
        }

        @Override
        public boolean accept(Packet packet) {
            for (PacketFilter filter : mFilters) {
                if (!filter.accept(packet)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public PacketFilter and(PacketFilter filter) {
            mFilters.add(filter);

            return this;
        }
    }

    private static class PacketFilterOr extends PacketFilter {
        private final LinkedList<PacketFilter> mFilters = new LinkedList<>();

        PacketFilterOr(PacketFilter lhs, PacketFilter rhs) {
            mFilters.add(lhs);
            mFilters.add(rhs);
        }

        @Override
        public boolean accept(Packet packet) {
            for (PacketFilter filter : mFilters) {
                if (filter.accept(packet)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public PacketFilter or(PacketFilter filter) {
            mFilters.add(filter);

            return this;
        }
    }
}