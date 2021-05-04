package org.zaproxy.zap.extension.domxss;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.httpclient.URI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.selenium.Browser;
import org.zaproxy.zap.testutils.ActiveScannerTestUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class DomXssScanRuleIntegrationTest extends ActiveScannerTestUtils<DomXssScanRule> {

    @BeforeAll
    static void setup() {
        WebDriverManager.firefoxdriver().setup();
        WebDriverManager.chromedriver().setup();
    }

    static Stream<String> testBrowsers() throws Exception {
        // TODO chrome-headless is failing in travis - need to investigate at some point
        return Stream.of("firefox-headless");
    }

    @AfterAll
    static void tidyUp() {
        DomXssScanRule.tidyUp();
    }

    @Override
    protected DomXssScanRule createScanner() {
        return new DomXssScanRule();
    }

    @Override
    protected void setUpMessages() {
        mockMessages(new ExtensionDomXSS());
    }

    Collection<DynamicTest> commonScanRuleTests() {
        return Collections.EMPTY_LIST;
    }

    /**
     * Test based on http://public-firing-range.appspot.com/address/location.hash/replace
     */
    @ParameterizedTest
    @MethodSource("testBrowsers")
    public void testSwaggerUiDomXss(String browser)
            throws Exception {

        HttpMessage msg = new HttpMessage(new URI("https://some-site/swagger-ui.html"));

        this.rule.getConfig().setProperty("rules.domxss.browserid", Browser.FIREFOX_HEADLESS.getId());
        this.rule.init(msg, this.parent);

        // When
        this.rule.scan();

        assertThat(alertsRaised.size(), equalTo(1));
    }
}
