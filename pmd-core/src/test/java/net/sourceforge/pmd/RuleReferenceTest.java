/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import net.sourceforge.pmd.lang.DummyLanguageModule;
import net.sourceforge.pmd.lang.Language;
import net.sourceforge.pmd.lang.rule.MockRule;
import net.sourceforge.pmd.lang.rule.RuleReference;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;

class RuleReferenceTest extends PmdContextualizedTest {

    @Test
    void testRuleSetReference() {
        RuleReference ruleReference = new RuleReference();
        RuleSetReference ruleSetReference = new RuleSetReference("somename");
        ruleReference.setRuleSetReference(ruleSetReference);
        assertEquals(ruleSetReference, ruleReference.getRuleSetReference(), "Not same rule set reference");
    }

    @Test
    void testOverride() {
        final PropertyDescriptor<String> PROPERTY1_DESCRIPTOR = PropertyFactory.stringProperty("property1").desc("Test property").defaultValue("").build();
        MockRule rule = new MockRule();
        rule.definePropertyDescriptor(PROPERTY1_DESCRIPTOR);
        Language dummyLang = DummyLanguageModule.getInstance();
        rule.setLanguage(dummyLang);
        rule.setName("name1");
        rule.setProperty(PROPERTY1_DESCRIPTOR, "value1");
        rule.setMessage("message1");
        rule.setDescription("description1");
        rule.addExample("example1");
        rule.setExternalInfoUrl("externalInfoUrl1");
        rule.setPriority(RulePriority.HIGH);

        final PropertyDescriptor<String> PROPERTY2_DESCRIPTOR = PropertyFactory.stringProperty("property2").desc("Test property").defaultValue("").build();
        RuleReference ruleReference = new RuleReference();
        ruleReference.setRule(rule);
        ruleReference.definePropertyDescriptor(PROPERTY2_DESCRIPTOR);
        ruleReference.setMinimumLanguageVersion(dummyLang.getVersion("1.3"));
        ruleReference.setMaximumLanguageVersion(dummyLang.getVersion("1.7"));
        ruleReference.setDeprecated(true);
        ruleReference.setName("name2");
        ruleReference.setProperty(PROPERTY1_DESCRIPTOR, "value2");
        ruleReference.setProperty(PROPERTY2_DESCRIPTOR, "value3");
        ruleReference.setMessage("message2");
        ruleReference.setDescription("description2");
        ruleReference.addExample("example2");
        ruleReference.setExternalInfoUrl("externalInfoUrl2");
        ruleReference.setPriority(RulePriority.MEDIUM_HIGH);

        validateOverriddenValues(PROPERTY1_DESCRIPTOR, PROPERTY2_DESCRIPTOR, ruleReference);
    }

    @Test
    void testLanguageOverrideDisallowed() {
        MockRule rule = new MockRule();
        Language dummyLang = DummyLanguageModule.getInstance();
        rule.setLanguage(dummyLang);

        RuleReference ruleReference = new RuleReference();
        ruleReference.setRule(rule);

        assertThrows(UnsupportedOperationException.class, () -> ruleReference.setLanguage(dummyLanguage2()));
        assertEquals(dummyLang, ruleReference.getLanguage());
        assertThrows(IllegalArgumentException.class, () -> ruleReference.setMaximumLanguageVersion(dummyLanguage2().getVersion("1.0")));
        assertEquals(rule.getMaximumLanguageVersion(), ruleReference.getOverriddenMaximumLanguageVersion());
        assertThrows(IllegalArgumentException.class, () -> ruleReference.setMinimumLanguageVersion(dummyLanguage2().getVersion("1.0")));
        assertEquals(rule.getMinimumLanguageVersion(), ruleReference.getMinimumLanguageVersion());
    }

    @Test
    void testDeepCopyOverride() {
        final PropertyDescriptor<String> PROPERTY1_DESCRIPTOR = PropertyFactory.stringProperty("property1").desc("Test property").defaultValue("").build();
        MockRule rule = new MockRule();
        rule.definePropertyDescriptor(PROPERTY1_DESCRIPTOR);
        Language dummyLang = DummyLanguageModule.getInstance();
        rule.setLanguage(dummyLang);
        rule.setName("name1");
        rule.setProperty(PROPERTY1_DESCRIPTOR, "value1");
        rule.setMessage("message1");
        rule.setDescription("description1");
        rule.addExample("example1");
        rule.setExternalInfoUrl("externalInfoUrl1");
        rule.setPriority(RulePriority.HIGH);

        final PropertyDescriptor<String> PROPERTY2_DESCRIPTOR = PropertyFactory.stringProperty("property2").desc("Test property").defaultValue("").build();
        RuleReference ruleReference = new RuleReference();
        ruleReference.setRule(rule);
        ruleReference.definePropertyDescriptor(PROPERTY2_DESCRIPTOR);
        ruleReference.setLanguage(dummyLang);
        ruleReference.setMinimumLanguageVersion(dummyLang.getVersion("1.3"));
        ruleReference.setMaximumLanguageVersion(dummyLang.getVersion("1.7"));
        ruleReference.setDeprecated(true);
        ruleReference.setName("name2");
        ruleReference.setProperty(PROPERTY1_DESCRIPTOR, "value2");
        ruleReference.setProperty(PROPERTY2_DESCRIPTOR, "value3");
        ruleReference.setMessage("message2");
        ruleReference.setDescription("description2");
        ruleReference.addExample("example2");
        ruleReference.setExternalInfoUrl("externalInfoUrl2");
        ruleReference.setPriority(RulePriority.MEDIUM_HIGH);

        validateOverriddenValues(PROPERTY1_DESCRIPTOR, PROPERTY2_DESCRIPTOR, (RuleReference) ruleReference.deepCopy());
    }

    private void validateOverriddenValues(final PropertyDescriptor<String> propertyDescriptor1,
            final PropertyDescriptor<String> propertyDescriptor2, RuleReference ruleReference) {
        assertEquals(DummyLanguageModule.getInstance(), ruleReference.getLanguage(),
                     "Override failed");

        assertEquals(DummyLanguageModule.getInstance().getVersion("1.3"), ruleReference.getMinimumLanguageVersion(),
                     "Override failed");
        assertEquals(DummyLanguageModule.getInstance().getVersion("1.3"), ruleReference.getOverriddenMinimumLanguageVersion(),
                     "Override failed");

        assertEquals(DummyLanguageModule.getInstance().getVersion("1.7"), ruleReference.getMaximumLanguageVersion(),
                     "Override failed");
        assertEquals(DummyLanguageModule.getInstance().getVersion("1.7"), ruleReference.getOverriddenMaximumLanguageVersion(),
                     "Override failed");

        assertEquals(false, ruleReference.getRule().isDeprecated(), "Override failed");
        assertEquals(true, ruleReference.isDeprecated(), "Override failed");
        assertEquals(true, ruleReference.isOverriddenDeprecated(), "Override failed");

        assertEquals("name2", ruleReference.getName(), "Override failed");
        assertEquals("name2", ruleReference.getOverriddenName(), "Override failed");

        assertEquals("value2", ruleReference.getProperty(propertyDescriptor1), "Override failed");
        assertEquals("value3", ruleReference.getProperty(propertyDescriptor2), "Override failed");
        assertTrue(ruleReference.getPropertyDescriptors().contains(propertyDescriptor1), "Override failed");
        assertTrue(ruleReference.getPropertyDescriptors().contains(propertyDescriptor2), "Override failed");
        assertFalse(ruleReference.getOverriddenPropertyDescriptors().contains(propertyDescriptor1), "Override failed");
        assertTrue(ruleReference.getOverriddenPropertyDescriptors().contains(propertyDescriptor2), "Override failed");
        assertTrue(ruleReference.getPropertiesByPropertyDescriptor().containsKey(propertyDescriptor1),
                "Override failed");
        assertTrue(ruleReference.getPropertiesByPropertyDescriptor().containsKey(propertyDescriptor2),
                "Override failed");
        assertTrue(ruleReference.getOverriddenPropertiesByPropertyDescriptor().containsKey(propertyDescriptor1),
                "Override failed");
        assertTrue(ruleReference.getOverriddenPropertiesByPropertyDescriptor().containsKey(propertyDescriptor2),
                "Override failed");

        assertEquals("message2", ruleReference.getMessage(), "Override failed");
        assertEquals("message2", ruleReference.getOverriddenMessage(), "Override failed");

        assertEquals("description2", ruleReference.getDescription(), "Override failed");
        assertEquals("description2", ruleReference.getOverriddenDescription(), "Override failed");

        assertEquals(2, ruleReference.getExamples().size(), "Override failed");
        assertEquals("example1", ruleReference.getExamples().get(0), "Override failed");
        assertEquals("example2", ruleReference.getExamples().get(1), "Override failed");
        assertEquals("example2", ruleReference.getOverriddenExamples().get(0), "Override failed");

        assertEquals("externalInfoUrl2", ruleReference.getExternalInfoUrl(), "Override failed");
        assertEquals("externalInfoUrl2", ruleReference.getOverriddenExternalInfoUrl(), "Override failed");

        assertEquals(RulePriority.MEDIUM_HIGH, ruleReference.getPriority(), "Override failed");
        assertEquals(RulePriority.MEDIUM_HIGH, ruleReference.getOverriddenPriority(), "Override failed");
    }

    @Test
    void testNotOverride() {
        final PropertyDescriptor<String> PROPERTY1_DESCRIPTOR = PropertyFactory.stringProperty("property1").desc("Test property").defaultValue("").build();
        MockRule rule = new MockRule();
        rule.definePropertyDescriptor(PROPERTY1_DESCRIPTOR);
        rule.setLanguage(DummyLanguageModule.getInstance());
        rule.setMinimumLanguageVersion(DummyLanguageModule.getInstance().getVersion("1.3"));
        rule.setMaximumLanguageVersion(DummyLanguageModule.getInstance().getVersion("1.7"));
        rule.setName("name1");
        rule.setProperty(PROPERTY1_DESCRIPTOR, "value1");
        rule.setMessage("message1");
        rule.setDescription("description1");
        rule.addExample("example1");
        rule.setExternalInfoUrl("externalInfoUrl1");
        rule.setPriority(RulePriority.HIGH);

        RuleReference ruleReference = new RuleReference();
        ruleReference.setRule(rule);
        ruleReference
            .setMinimumLanguageVersion(DummyLanguageModule.getInstance().getVersion("1.3"));
        ruleReference
            .setMaximumLanguageVersion(DummyLanguageModule.getInstance().getVersion("1.7"));
        ruleReference.setDeprecated(false);
        ruleReference.setName("name1");
        ruleReference.setProperty(PROPERTY1_DESCRIPTOR, "value1");
        ruleReference.setMessage("message1");
        ruleReference.setDescription("description1");
        ruleReference.addExample("example1");
        ruleReference.setExternalInfoUrl("externalInfoUrl1");
        ruleReference.setPriority(RulePriority.HIGH);


        assertEquals(DummyLanguageModule.getInstance().getVersion("1.3"), ruleReference.getMinimumLanguageVersion(),
                     "Override failed");
        assertNull(ruleReference.getOverriddenMinimumLanguageVersion(), "Override failed");

        assertEquals(DummyLanguageModule.getInstance().getVersion("1.7"), ruleReference.getMaximumLanguageVersion(),
                     "Override failed");
        assertNull(ruleReference.getOverriddenMaximumLanguageVersion(), "Override failed");

        assertEquals(false, ruleReference.isDeprecated(), "Override failed");
        assertNull(ruleReference.isOverriddenDeprecated(), "Override failed");

        assertEquals("name1", ruleReference.getName(), "Override failed");
        assertNull(ruleReference.getOverriddenName(), "Override failed");

        assertEquals("value1", ruleReference.getProperty(PROPERTY1_DESCRIPTOR), "Override failed");

        assertEquals("message1", ruleReference.getMessage(), "Override failed");
        assertNull(ruleReference.getOverriddenMessage(), "Override failed");

        assertEquals("description1", ruleReference.getDescription(), "Override failed");
        assertNull(ruleReference.getOverriddenDescription(), "Override failed");

        assertEquals(1, ruleReference.getExamples().size(), "Override failed");
        assertEquals("example1", ruleReference.getExamples().get(0), "Override failed");
        assertNull(ruleReference.getOverriddenExamples(), "Override failed");

        assertEquals("externalInfoUrl1", ruleReference.getExternalInfoUrl(), "Override failed");
        assertNull(ruleReference.getOverriddenExternalInfoUrl(), "Override failed");

        assertEquals(RulePriority.HIGH, ruleReference.getPriority(), "Override failed");
        assertNull(ruleReference.getOverriddenPriority(), "Override failed");
    }
}
