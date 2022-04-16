/*
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd;

import static net.sourceforge.pmd.util.CollectionUtil.buildMap;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.Before;
import org.junit.contrib.java.lang.system.SystemErrRule;
import org.mockito.verification.VerificationMode;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import net.sourceforge.pmd.junit.LocaleRule;
import net.sourceforge.pmd.lang.DummyLanguageModule;
import net.sourceforge.pmd.util.internal.xml.SchemaConstant;
import net.sourceforge.pmd.util.internal.xml.SchemaConstants;
import net.sourceforge.pmd.util.log.MessageReporter;
import net.sourceforge.pmd.util.log.internal.SimpleMessageReporter;

public class RulesetFactoryTestBase {

    @org.junit.Rule
    public LocaleRule localeRule = LocaleRule.en();

    @org.junit.Rule
    public SystemErrRule systemErrRule = new SystemErrRule().muteForSuccessfulTests();

    protected MessageReporter mockReporter;

    @Before
    public void setup() {
        SimpleMessageReporter reporter = new SimpleMessageReporter(LoggerFactory.getLogger(RulesetFactoryTestBase.class));
        mockReporter = spy(reporter);
    }

    protected void verifyNoWarnings() {
        verifyZeroInteractions(mockReporter);
    }

    protected static Predicate<String> containing(String part) {
        return new Predicate<String>() {
            @Override
            public boolean test(String it) {
                return it.contains(part);
            }

            @Override
            public String toString() {
                return "string containing: " + part;
            }
        };
    }

    protected void verifyFoundAWarningWithMessage(Predicate<String> messageTest) {
        verifyFoundWarningWithMessage(times(1), messageTest);
    }

    protected void verifyFoundWarningWithMessage(VerificationMode mode, Predicate<String> messageTest) {
        verify(mockReporter, mode)
            .logEx(eq(Level.WARN), argThat(messageTest::test), any(), any());
    }

    protected void verifyFoundAnErrorWithMessage(Predicate<String> messageTest) {
        verify(mockReporter, times(1))
            .logEx(eq(Level.ERROR), argThat(messageTest::test), any(), any());
    }


    protected RuleSet loadRuleSetInDir(String resourceDir, String ruleSetFilename) {
        RuleSetLoader loader = new RuleSetLoader();
        loader.setReporter(mockReporter);
        return loader.loadFromResource(resourceDir + "/" + ruleSetFilename);
    }

    protected static @NonNull String rulesetXml(String... contents) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "\n"
            + "<ruleset name=\"Custom ruleset\" xmlns=\"http://pmd.sourceforge.net/ruleset/2.0.0\"\n"
            + "    xmlns:xsi=\"http:www.w3.org/2001/XMLSchema-instance\"\n"
            + "    xsi:schemaLocation=\"http://pmd.sourceforge.net/ruleset/2.0.0 https://pmd.sourceforge.io/ruleset_2_0_0.xsd\">\n"
            + "    <description>Ruleset which references a empty ruleset</description>\n" + "\n"
            + body(contents)
            + "</ruleset>\n";
    }

    protected static @NonNull String ruleRef(String ref) {
        return "<rule ref=\"" + ref + "\"/>\n";
    }

    protected static @NonNull String rule(Map<SchemaConstant, String> attrs, String... body) {
        return "<rule " + attrs(attrs) + ">\n"
            + body(body)
            + "</rule>";
    }

    protected static @NonNull String dummyRule(Consumer<Map<SchemaConstant, String>> attributes, String... body) {
        return rule(buildMap(dummyRuleDefAttrs(), attributes), body);
    }

    protected static Map<SchemaConstant, String> dummyRuleDefAttrs() {
        return buildMap(
            map -> {
                map.put(SchemaConstants.NAME, "MockRuleName");
                map.put(SchemaConstants.LANGUAGE, DummyLanguageModule.TERSE_NAME);
                map.put(SchemaConstants.CLASS, net.sourceforge.pmd.lang.rule.MockRule.class.getName());
                map.put(SchemaConstants.MESSAGE, "avoid the mock rule");
            }
        );
    }

    private static @NonNull String attrs(Map<SchemaConstant, String> str) {
        return str.entrySet().stream()
                  .map(it -> it.getKey().xmlName() + "=\"" + it.getValue() + "\"")
                  .collect(Collectors.joining(" "));
    }


    protected static @NonNull String rulesetRef(String ref, String... body) {
        return ruleRef(ref, body);
    }

    protected static @NonNull String ruleRef(String ref, String... body) {
        return "<rule ref=\"" + ref + "\">\n"
            + body(body)
            + "</rule>\n";
    }

    protected static @NonNull String excludePattern(String pattern) {
        return tagOneLine("exclude-pattern", pattern);
    }

    protected static @NonNull String excludeRule(String name) {
        return emptyTag("exclude", buildMap(map -> map.put(SchemaConstants.NAME, name)));
    }

    protected static @NonNull String includePattern(String pattern) {
        return tagOneLine("include-pattern", pattern);
    }

    protected static @NonNull String priority(String prio) {
        return tagOneLine("priority", prio);
    }

    protected static @NonNull String description(String description) {
        return tagOneLine("description", description);
    }

    protected static @NonNull String body(String... lines) {
        return String.join("\n", lines);
    }

    protected static @NonNull String properties(String... body) {
        return tag("properties", body);
    }

    protected static @NonNull String propertyWithValueAttr(String name, String valueAttr) {
        return "<property name='" + name + "' value='" + valueAttr + "/>\n";
    }

    protected static @NonNull String propertyDefWithValueAttr(String name,
                                                              String description,
                                                              String type,
                                                              String valueAttr) {
        return emptyTag("property", buildMap(
            map -> {
                map.put(SchemaConstants.NAME, name);
                map.put(SchemaConstants.DESCRIPTION, description);
                map.put(SchemaConstants.PROPERTY_TYPE, type);
                map.put(SchemaConstants.PROPERTY_VALUE, valueAttr);
            }
        ));
    }

    private static @NonNull String tag(String tagName, String... body) {
        return "<" + tagName + ">\n"
            + body(body)
            + "</" + tagName + ">";
    }

    private static @NonNull String emptyTag(String tagName, Map<SchemaConstant, String> attrs) {
        return "<" + tagName + " " + attrs(attrs) + " />";
    }

    private static @NonNull String tagOneLine(String tagName, String text) {
        return "<" + tagName + ">" + text + "</" + tagName + ">";
    }
}
