/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.inferred.freebuilder.processor;

import com.google.common.base.Preconditions;

import org.inferred.freebuilder.FreeBuilder;
import org.inferred.freebuilder.processor.util.testing.BehaviorTester;
import org.inferred.freebuilder.processor.util.testing.SourceBuilder;
import org.inferred.freebuilder.processor.util.testing.TestBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

@RunWith(JUnit4.class)
public class OptionalMapperMethodTest {

  private static final JavaFileObject J8_OPTIONAL_INTEGER_TYPE = new SourceBuilder()
      .addLine("package com.example;")
      .addLine("@%s", FreeBuilder.class)
      .addLine("public interface DataType {")
      .addLine("  %s<Integer> getProperty();", java.util.Optional.class)
      .addLine("")
      .addLine("  public static class Builder extends DataType_Builder {}")
      .addLine("}")
      .build();

  private static final JavaFileObject GUAVA_OPTIONAL_INTEGER_TYPE = new SourceBuilder()
      .addLine("package com.example;")
      .addLine("@%s", FreeBuilder.class)
      .addLine("public interface DataType {")
      .addLine("  %s<Integer> getProperty();", com.google.common.base.Optional.class)
      .addLine("")
      .addLine("  public static class Builder extends DataType_Builder {}")
      .addLine("}")
      .build();

  @Rule public final ExpectedException thrown = ExpectedException.none();
  private final BehaviorTester behaviorTester = new BehaviorTester();

  @Test
  public void mapReplacesValueToBeReturnedFromGetterForJ8OptionalProperty() {
    behaviorTester
        .with(new Processor())
        .with(J8_OPTIONAL_INTEGER_TYPE)
        .with(new TestBuilder()
            .addLine("com.example.DataType value = new com.example.DataType.Builder()")
            .addLine("    .setProperty(11)")
            .addLine("    .mapProperty(a -> a + 3)")
            .addLine("    .build();")
            .addLine("assertEquals(14, (int) value.getProperty().get());")
            .build())
        .runTest();
  }

  @Test
  public void mapReplacesValueToBeReturnedFromGetterForGuavaOptionalProperty() {
    behaviorTester
        .with(new Processor())
        .with(GUAVA_OPTIONAL_INTEGER_TYPE)
        .with(new TestBuilder()
            .addLine("com.example.DataType value = new com.example.DataType.Builder()")
            .addLine("    .setProperty(11)")
            .addLine("    .mapProperty(a -> a + 3)")
            .addLine("    .build();")
            .addLine("assertEquals(14, (int) value.getProperty().get());")
            .build())
        .runTest();
  }

  @Test
  public void mapDelegatesToSetterForValidationForJ8OptionalProperty() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("property must be non-negative");
    behaviorTester
        .with(new Processor())
        .with(new SourceBuilder()
            .addLine("package com.example;")
            .addLine("@%s", FreeBuilder.class)
            .addLine("public interface DataType {")
            .addLine("  %s<Integer> getProperty();", java.util.Optional.class)
            .addLine("")
            .addLine("  public static class Builder extends DataType_Builder {")
            .addLine("    @Override public Builder setProperty(int property) {")
            .addLine("      %s.checkArgument(property >= 0, \"property must be non-negative\");",
                Preconditions.class)
            .addLine("      return super.setProperty(property);")
            .addLine("    }")
            .addLine("  }")
            .addLine("}")
            .build())
        .with(new TestBuilder()
            .addLine("new com.example.DataType.Builder()")
            .addLine("    .setProperty(11)")
            .addLine("    .mapProperty(a -> -3);")
            .build())
        .runTest();
  }

  @Test
  public void mapDelegatesToSetterForValidationForGuavaOptionalProperty() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("property must be non-negative");
    behaviorTester
        .with(new Processor())
        .with(new SourceBuilder()
            .addLine("package com.example;")
            .addLine("@%s", FreeBuilder.class)
            .addLine("public interface DataType {")
            .addLine("  %s<Integer> getProperty();", com.google.common.base.Optional.class)
            .addLine("")
            .addLine("  public static class Builder extends DataType_Builder {")
            .addLine("    @Override public Builder setProperty(int property) {")
            .addLine("      %s.checkArgument(property >= 0, \"property must be non-negative\");",
                Preconditions.class)
            .addLine("      return super.setProperty(property);")
            .addLine("    }")
            .addLine("  }")
            .addLine("}")
            .build())
        .with(new TestBuilder()
            .addLine("new com.example.DataType.Builder()")
            .addLine("    .setProperty(11)")
            .addLine("    .mapProperty(a -> -3);")
            .build())
        .runTest();
  }

  @Test
  public void mapThrowsNpeIfMapperIsNullForJ8OptionalProperty() {
    thrown.expect(NullPointerException.class);
    behaviorTester
        .with(new Processor())
        .with(J8_OPTIONAL_INTEGER_TYPE)
        .with(new TestBuilder()
            .addLine("new com.example.DataType.Builder()")
            .addLine("    .setProperty(11)")
            .addLine("    .mapProperty(null);")
            .build())
        .runTest();
  }

  @Test
  public void mapThrowsNpeIfMapperIsNullForGuavaOptionalProperty() {
    thrown.expect(NullPointerException.class);
    behaviorTester
        .with(new Processor())
        .with(GUAVA_OPTIONAL_INTEGER_TYPE)
        .with(new TestBuilder()
            .addLine("new com.example.DataType.Builder()")
            .addLine("    .setProperty(11)")
            .addLine("    .mapProperty(null);")
            .build())
        .runTest();
  }

  @Test
  public void mapThrowsNpeIfMapperIsNullForEmptyJ8OptionalProperty() {
    thrown.expect(NullPointerException.class);
    behaviorTester
        .with(new Processor())
        .with(J8_OPTIONAL_INTEGER_TYPE)
        .with(new TestBuilder()
            .addLine("new com.example.DataType.Builder()")
            .addLine("    .mapProperty(null);")
            .build())
        .runTest();
  }

  @Test
  public void mapThrowsNpeIfMapperIsNullForAbsentGuavaOptionalProperty() {
    thrown.expect(NullPointerException.class);
    behaviorTester
        .with(new Processor())
        .with(GUAVA_OPTIONAL_INTEGER_TYPE)
        .with(new TestBuilder()
            .addLine("new com.example.DataType.Builder()")
            .addLine("    .mapProperty(null);")
            .build())
        .runTest();
  }

  @Test
  public void mapAllowsNullReturnForJ8OptionalProperty() {
    behaviorTester
        .with(new Processor())
        .with(J8_OPTIONAL_INTEGER_TYPE)
        .with(new TestBuilder()
            .addLine("com.example.DataType value = new com.example.DataType.Builder()")
            .addLine("    .setProperty(11)")
            .addLine("    .mapProperty(a -> null)")
            .addLine("    .build();")
            .addLine("assertFalse(value.getProperty().isPresent());")
            .build())
        .runTest();
  }

  @Test
  public void mapAllowsNullReturnForGuavaOptionalProperty() {
    behaviorTester
        .with(new Processor())
        .with(GUAVA_OPTIONAL_INTEGER_TYPE)
        .with(new TestBuilder()
            .addLine("com.example.DataType value = new com.example.DataType.Builder()")
            .addLine("    .setProperty(11)")
            .addLine("    .mapProperty(a -> null)")
            .addLine("    .build();")
            .addLine("assertFalse(value.getProperty().isPresent());")
            .build())
        .runTest();
  }

  @Test
  public void mapSkipsMapperIfJ8OptionalPropertyIsEmpty() {
    behaviorTester
        .with(new Processor())
        .with(J8_OPTIONAL_INTEGER_TYPE)
        .with(new TestBuilder()
            .addLine("com.example.DataType value = new com.example.DataType.Builder()")
            .addLine("    .mapProperty(a -> { fail(\"mapper called\"); return null; })")
            .addLine("    .build();")
            .addLine("assertFalse(value.getProperty().isPresent());")
            .build())
        .runTest();
  }

  @Test
  public void mapSkipsMapperIfGuavaOptionalPropertyIsAbsent() {
    behaviorTester
        .with(new Processor())
        .with(GUAVA_OPTIONAL_INTEGER_TYPE)
        .with(new TestBuilder()
            .addLine("com.example.DataType value = new com.example.DataType.Builder()")
            .addLine("    .mapProperty(a -> { fail(\"mapper called\"); return null; })")
            .addLine("    .build();")
            .addLine("assertFalse(value.getProperty().isPresent());")
            .build())
        .runTest();
  }
}
