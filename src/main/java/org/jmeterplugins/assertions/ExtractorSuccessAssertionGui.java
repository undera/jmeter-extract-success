package org.jmeterplugins.assertions;

import kg.apc.jmeter.JMeterPluginsUtils;
import org.apache.jmeter.assertions.gui.AbstractAssertionGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledTextField;

import javax.swing.*;
import java.awt.*;

public class ExtractorSuccessAssertionGui extends AbstractAssertionGui {
    private static final String WIKIPAGE = "https://github.com/undera/jmeter-extract-success";

    private static final long serialVersionUID = 1L;
    private JCheckBox handleStandard = null;
    private JLabeledTextField defaultVals = null;

    public ExtractorSuccessAssertionGui() {
        init();
    }

    public void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        add(JMeterPluginsUtils.addHelpLinkToPanel(makeTitlePanel(), WIKIPAGE), BorderLayout.NORTH);

        VerticalPanel panel = new VerticalPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        defaultVals = new JLabeledTextField("Default values to check among variables (comma-separated list): ");
        handleStandard = new JCheckBox("Find defaults among standard extractors (RegEx, Boundary, CSS, JSON)");

        panel.add(handleStandard);
        panel.add(defaultVals);

        add(panel, BorderLayout.CENTER);
    }

    @Override
    public void clearGui() {
        super.clearGui();
        defaultVals.setText("");
        handleStandard.setSelected(true);
    }

    @Override
    public TestElement createTestElement() {
        ExtractorSuccessAssertion jpAssertion = new ExtractorSuccessAssertion();
        modifyTestElement(jpAssertion);
        jpAssertion.setComment(JMeterPluginsUtils.getWikiLinkText(WIKIPAGE));
        return jpAssertion;
    }

    @Override
    public String getLabelResource() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getStaticLabel() {
        return JMeterPluginsUtils.prefixLabel("Extractor Success Assertion");
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
        if (element instanceof ExtractorSuccessAssertion) {
            ExtractorSuccessAssertion jpAssertion = (ExtractorSuccessAssertion) element;
            jpAssertion.setDefaultValues(defaultVals.getText());
            jpAssertion.setHandleStandardExtractors(handleStandard.isSelected());
        }
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        ExtractorSuccessAssertion jpAssertion = (ExtractorSuccessAssertion) element;
        defaultVals.setText(jpAssertion.getDefaultValues());
        handleStandard.setSelected(jpAssertion.isHandleStandardExtractors());
    }
}