// Copyright 2016 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.devtools.build.android.desugar;

import java.io.IOException;
import java.io.InputStream;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

/** Utility class to prefix or unprefix class names of core library classes */
class CoreLibraryRewriter {
  public CoreLibraryRewriter(String prefix) {
    this.prefix = prefix;
  }

  public ClassReader reader(InputStream content) throws IOException {
    if (!"".equals(prefix)) {
      return new Reader(content);
    } else {
      return new ClassReader(content);
    }
  }

  public Writer writer(int flags) {
    return new Writer(flags);
  }

  public static boolean shouldPrefix(String typeName) {
    return typeName.startsWith("java/") || typeName.startsWith("sun/");
  }

  public static boolean except(String typeName) {
    if (typeName.startsWith("java/lang/invoke/")) {
      return true;
    }

    switch (typeName) {
        // Autoboxed types
      case "java/lang/Boolean":
      case "java/lang/Byte":
      case "java/lang/Character":
      case "java/lang/Double":
      case "java/lang/Float":
      case "java/lang/Integer":
      case "java/lang/Long":
      case "java/lang/Number":
      case "java/lang/Short":

        // Special types
      case "java/lang/Class":
      case "java/lang/Object":
      case "java/lang/String":
      case "java/lang/Throwable":
        return true;

      default: // fall out
    }

    return false;
  }

  public String prefix(String typeName) {
    if (shouldPrefix(typeName) && !except(typeName)) {
      return prefix + typeName;
    }
    return typeName;
  }

  public String unprefix(String typeName) {
    if (!typeName.startsWith(prefix)) {
      return typeName;
    }
    return typeName.substring(prefix.length());
  }

  private final String prefix;

  private class Reader extends ClassReader {
    Reader(InputStream content) throws IOException {
      super(content);
    }

    @Override
    public void accept(ClassVisitor cv, Attribute[] attrs, int flags) {
      cv =
          new ClassRemapper(
              cv,
              new Remapper() {
                @Override
                public String map(String typeName) {
                  return prefix(typeName);
                }
              });
      super.accept(cv, attrs, flags);
    }
  }

  public class Writer extends ClassVisitor {
    private final ClassWriter writer;

    Writer(int flags) {
      super(Opcodes.ASM5);
      this.writer = new ClassWriter(flags);
      this.cv = this.writer;
      if (!"".equals(prefix)) {
        this.cv =
            new ClassRemapper(
                this.cv,
                new Remapper() {
                  @Override
                  public String map(String typeName) {
                    return unprefix(typeName);
                  }
                });
      }
    }

    byte[] toByteArray() {
      return writer.toByteArray();
    }
  }
}
