LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := desugar
LOCAL_SRC_FILES := $(call all-java-files-under, java)
LOCAL_JAR_MANIFEST := manifest.txt
LOCAL_STATIC_JAVA_LIBRARIES := \
    asm-5.2 \
    asm-commons-5.2 \
    asm-tree-5.2 \
    guava-20.0 \
    jsr305-3.0.1 \
    dagger2-auto-value-host \

include $(BUILD_HOST_JAVA_LIBRARY)
