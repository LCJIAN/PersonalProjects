package org.gradle.mesh.packet;

public abstract class IQ extends Packet {

    private Type type = Type.GET;

    /**
     * Returns the type of the IQ packet.
     *
     * @return the type of the IQ packet.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of the IQ packet.
     *
     * @param type the type of the IQ packet.
     */
    public void setType(Type type) {
        if (type == null) {
            this.type = Type.GET;
        } else {
            this.type = type;
        }
    }

    public static class Type {

        public static final Type GET = new Type("get");
        public static final Type SET = new Type("set");
        public static final Type RESULT = new Type("result");
        public static final Type ERROR = new Type("error");
        private String value;

        private Type(String value) {
            this.value = value;
        }

        /**
         * Converts a String into the corresponding types. Valid String values
         * that can be converted to types are: "get", "set", "result", and "error".
         *
         * @param type the String value to covert.
         * @return the corresponding Type.
         */
        public static Type fromString(String type) {
            if (type == null) {
                return null;
            }
            type = type.toLowerCase();
            if (GET.toString().equals(type)) {
                return GET;
            } else if (SET.toString().equals(type)) {
                return SET;
            } else if (ERROR.toString().equals(type)) {
                return ERROR;
            } else if (RESULT.toString().equals(type)) {
                return RESULT;
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
