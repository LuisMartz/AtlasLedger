package atlasledger.service;

import atlasledger.model.Informe;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReportSnapshot {

    private final Informe informe;
    private final Map<String, Number> summary;
    private final Map<String, Number> breakdown;
    private final String breakdownTitle;

    public ReportSnapshot(Informe informe,
                          Map<String, Number> summary,
                          Map<String, Number> breakdown,
                          String breakdownTitle) {
        this.informe = informe;
        this.summary = Collections.unmodifiableMap(new LinkedHashMap<>(summary));
        this.breakdown = Collections.unmodifiableMap(new LinkedHashMap<>(breakdown));
        this.breakdownTitle = breakdownTitle;
    }

    public Informe getInforme() {
        return informe;
    }

    public Map<String, Number> getSummary() {
        return summary;
    }

    public Map<String, Number> getBreakdown() {
        return breakdown;
    }

    public String getBreakdownTitle() {
        return breakdownTitle;
    }
}
