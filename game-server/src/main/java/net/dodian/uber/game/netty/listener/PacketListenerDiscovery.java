package net.dodian.uber.game.netty.listener;

import com.google.common.reflect.ClassPath;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Discovers and validates inbound packet listeners without relying on class initializers. */
public final class PacketListenerDiscovery {
    public static final String INBOUND_PACKAGE = "net.dodian.uber.game.netty.listener.in";

    private PacketListenerDiscovery() {
    }

    public static Result discover(ClassLoader classLoader) throws IOException {
        List<Class<?>> classes = new ArrayList<>();
        List<ClassPath.ClassInfo> infos = new ArrayList<>(
                ClassPath.from(classLoader).getTopLevelClassesRecursive(INBOUND_PACKAGE));
        infos.sort(Comparator.comparing(ClassPath.ClassInfo::getName));
        for (ClassPath.ClassInfo info : infos) {
            try {
                classes.add(Class.forName(info.getName(), false, classLoader));
            } catch (ClassNotFoundException exception) {
                throw new IllegalStateException("Discovered packet listener class cannot be loaded: " + info.getName(), exception);
            }
        }
        return validateAndInstantiate(classes);
    }

    static Result validateAndInstantiate(Collection<Class<?>> candidates) {
        List<Class<?>> ordered = new ArrayList<>(candidates);
        ordered.sort(Comparator.comparing(Class::getName));
        List<Binding> bindings = new ArrayList<>();
        Map<Integer, String> owners = new LinkedHashMap<>();
        int handlerCount = 0;

        for (Class<?> candidate : ordered) {
            PacketHandler annotation = candidate.getAnnotation(PacketHandler.class);
            boolean concreteListener = PacketListener.class.isAssignableFrom(candidate)
                    && !candidate.isInterface()
                    && !Modifier.isAbstract(candidate.getModifiers());
            if (!concreteListener) {
                if (annotation != null) {
                    throw new IllegalStateException("@PacketHandler class must be a concrete PacketListener: " + candidate.getName());
                }
                continue;
            }
            if (annotation == null) {
                throw new IllegalStateException("Concrete PacketListener is missing @PacketHandler: " + candidate.getName());
            }
            if (!Modifier.isPublic(candidate.getModifiers())) {
                throw new IllegalStateException("Packet listener must be public: " + candidate.getName());
            }
            int[] opcodes = annotation.opcodes().clone();
            if (opcodes.length == 0) {
                throw new IllegalStateException("Packet listener declares no opcodes: " + candidate.getName());
            }
            Arrays.sort(opcodes);
            for (int index = 0; index < opcodes.length; index++) {
                int opcode = opcodes[index];
                if (opcode < 0 || opcode > 255) {
                    throw new IllegalStateException("Packet listener opcode out of range: " + opcode + " owner=" + candidate.getName());
                }
                if (index > 0 && opcodes[index - 1] == opcode) {
                    throw new IllegalStateException("Packet listener declares duplicate opcode " + opcode + ": " + candidate.getName());
                }
                String previous = owners.putIfAbsent(opcode, candidate.getName());
                if (previous != null) {
                    throw new IllegalStateException("Conflicting packet listener for opcode " + opcode + ": " + previous + " vs " + candidate.getName());
                }
            }

            PacketListener listener = instantiate(candidate);
            handlerCount++;
            for (int opcode : opcodes) {
                bindings.add(new Binding(opcode, listener, candidate.getName()));
            }
        }
        bindings.sort(Comparator.comparingInt(Binding::opcode).thenComparing(Binding::owner));
        Map<Integer, String> sortedOwners = new LinkedHashMap<>();
        bindings.forEach(binding -> sortedOwners.put(binding.opcode(), binding.owner()));
        return new Result(handlerCount, bindings, sortedOwners, fingerprint(bindings));
    }

    private static PacketListener instantiate(Class<?> type) {
        try {
            Constructor<?> constructor = type.getConstructor();
            return (PacketListener) constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Packet listener requires a public no-argument constructor: " + type.getName(), exception);
        }
    }

    private static String fingerprint(List<Binding> bindings) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (Binding binding : bindings) {
                digest.update((binding.opcode() + "=" + binding.owner() + "\n").getBytes(StandardCharsets.UTF_8));
            }
            StringBuilder output = new StringBuilder();
            for (byte value : digest.digest()) output.append(String.format("%02x", value & 0xff));
            return output.toString();
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    public static final class Binding {
        private final int opcode;
        private final PacketListener listener;
        private final String owner;

        private Binding(int opcode, PacketListener listener, String owner) {
            this.opcode = opcode;
            this.listener = listener;
            this.owner = owner;
        }

        public int opcode() { return opcode; }
        public PacketListener listener() { return listener; }
        public String owner() { return owner; }
    }

    public static final class Result {
        private final int handlerCount;
        private final List<Binding> bindings;
        private final Map<Integer, String> owners;
        private final String fingerprint;

        private Result(int handlerCount, List<Binding> bindings, Map<Integer, String> owners, String fingerprint) {
            this.handlerCount = handlerCount;
            this.bindings = Collections.unmodifiableList(new ArrayList<>(bindings));
            this.owners = Collections.unmodifiableMap(new LinkedHashMap<>(owners));
            this.fingerprint = fingerprint;
        }

        public int handlerCount() { return handlerCount; }
        public List<Binding> bindings() { return bindings; }
        public Map<Integer, String> owners() { return owners; }
        public String fingerprint() { return fingerprint; }
    }
}
