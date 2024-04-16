package org.ecocompass.api.utility;

import java.util.Arrays;
import java.util.Objects;

public class Traffic {
    private final double[] start;
    private final double[] end;
    private final String description;

    public Traffic(double[] start, double[] end, String description) {
        this.start = start;
        this.end = end;
        this.description = description;
    }

    public double[] getStart() {
        return start;
    }

    public double[] getEnd() {
        return end;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Traffic traffic = (Traffic) o;
        return Objects.equals(description, traffic.description);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(description);
        result = 31 * result + Arrays.hashCode(getStart());
        result = 31 * result + Arrays.hashCode(getEnd());
        return result;
    }
}
