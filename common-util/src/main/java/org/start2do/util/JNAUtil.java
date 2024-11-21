package org.start2do.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JNAUtil {

    public static <T extends Structure> T newStructure(T instance, Pointer pointer) {
        Pointer pInstance = instance.getPointer();
        pInstance.write(0L, pointer.getByteArray(0L, instance.size()), 0, instance.size());
        instance.read();
        return instance;
    }

    public static void free(Structure structure) {
        free(structure.getPointer());
    }

    public static void free(Pointer pointer) {
        Native.free(Pointer.nativeValue(pointer));
        Pointer.nativeValue(pointer, 0L);
    }

}
