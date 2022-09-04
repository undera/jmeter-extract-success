package org.jmeterplugins.assertions;

import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.extractor.BoundaryExtractor;
import org.apache.jmeter.extractor.HtmlExtractor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.extractor.json.jsonpath.JSONPostProcessor;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.SamplePackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;


public class ExtractorSuccessAssertion extends AbstractTestElement implements Serializable, Assertion, LoopIterationListener {
    private static final Logger log = LoggerFactory.getLogger(ExtractorSuccessAssertion.class);
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_VALS = "DefaultVals";
    private final Map<String, String> varCache;

    public ExtractorSuccessAssertion() {
        this.varCache = new HashMap<>();
    }

    @Override
    public AssertionResult getResult(SampleResult samplerResult) {
        JMeterContext threadContext = getThreadContext();
        JMeterVariables threadVars = threadContext.getVariables();
        Set<String> defaultValuesToCheck = this.getDefaultValuesSet();

        if (true) { // TODO
            SamplePackage pack = (SamplePackage) threadVars.getObject(JMeterThread.PACKAGE_OBJECT);
            List<PostProcessor> postProcessors = pack.getPostProcessors();
            // handle known extractor types (guessing by *Extractor suffix is not viable)
            for (PostProcessor proc : postProcessors) {
                if (proc instanceof RegexExtractor) {
                    String val = ((RegexExtractor) proc).getDefaultValue();
                    String refName = ((RegexExtractor) proc).getRefName();
                    AssertionResult varsContain = this.varsContain(threadVars, refName, val);
                    if (varsContain != null) {
                        return varsContain;
                    }
                } else if (proc instanceof BoundaryExtractor) {
                    String val = ((BoundaryExtractor) proc).getDefaultValue();
                    String refName = ((BoundaryExtractor) proc).getRefName();
                    AssertionResult varsContain = this.varsContain(threadVars, refName, val);
                    if (varsContain != null) {
                        return varsContain;
                    }
                } else if (proc instanceof HtmlExtractor) {
                    String val = ((HtmlExtractor) proc).getDefaultValue();
                    String refName = ((HtmlExtractor) proc).getRefName();
                    AssertionResult varsContain = this.varsContain(threadVars, refName, val);
                    if (varsContain != null) {
                        return varsContain;
                    }
                } else if (proc instanceof JSONPostProcessor) {
                    String[] vals = ((JSONPostProcessor) proc).getDefaultValues().split(";");
                    String[] refNames = ((JSONPostProcessor) proc).getRefNames().split(";");
                    if (vals.length == refNames.length) { // should be handled by JSONPostProcessor anyway...
                        for (int i = 0; i < refNames.length; i++) {
                            AssertionResult varsContain = this.varsContain(threadVars, refNames[i], vals[i]);
                            if (varsContain != null) {
                                return varsContain;
                            }
                        }
                    }
                }
                // TODO: more types to handle?
            }
        }

        if (true) {
            for (Map.Entry<String, Object> var : threadVars.entrySet()) {
                log.info("var " + var.toString());
                // check all variables as the laziest option
                String val = var.getValue().toString();
                String cached = varCache.getOrDefault(var.getKey(), "basically, a long string that should not match");
                if (cached.equals(val)) {
                    continue; // quick speed-up
                }

                varCache.put(var.getKey(), val);

                if (defaultValuesToCheck.contains(val)) {
                    log.warn("Triggered on " + var);
                    return getResult("Variable " + var.getKey() + " has default value: " + val);
                } else {
                    log.info("Var is fine: " + var.getKey() + "=" + val);
                }
            }
        }

        // guess by "extractor" suffix
        // cache already checked
        // check variable is defined
        // check variable is empty

        return getResult(""); // TODO: accumulate messages and report all at once?
    }

    private AssertionResult varsContain(JMeterVariables threadVars, String refName, String val) {
        if (threadVars.get(refName).equals(val)) {
            return getResult("Variable " + refName + " has default value: " + val);
        }

        return null;
    }

    public String getDefaultValues() {
        return getPropertyAsString(DEFAULT_VALS);
    }

    public void setDefaultValues(String values) {
        setProperty(DEFAULT_VALS, values);
    }

    public Set<String> getDefaultValuesSet() {
        log.info("Default vals: " + getDefaultValues());
        String[] split = getDefaultValues().split(",");
        return new HashSet<>(Arrays.asList(split));
    }

    private AssertionResult getResult(String msg) {
        AssertionResult result = new AssertionResult(getName());
        result.setFailure(!msg.equals(""));
        result.setFailureMessage(msg);
        return result;
    }

    @Override
    public void iterationStart(LoopIterationEvent iterEvent) {
        this.varCache.clear();
    }
}
