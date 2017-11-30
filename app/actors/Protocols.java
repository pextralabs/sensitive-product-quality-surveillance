package actors;

public class Protocols {

    private Protocols() {
        throw new IllegalStateException("Utility class");
    }

    public static class Operation {

        public enum Type {
            INSERT,
            UPDATE,
            DELETE
        }

        Object obj;
        Type type;

        public Operation(Object obj, Type type) {
            this.obj = obj;
            this.type = type;
        }
    }

    public static class Subscribe { }
    public static class Unsubscribe { }
}
