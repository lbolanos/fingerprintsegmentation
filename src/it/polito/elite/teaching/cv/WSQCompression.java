package it.polito.elite.teaching.cv;

public enum WSQCompression {
    WSQ_5_1(2.25D),
    WSQ_10_1(1.5D),
    WSQ_15_1(0.75D),
    WSQ_20_1(0.5D);

    double compression;

    private WSQCompression(double compression) {
        this.compression = compression;
    }

    public double getCompression() {
        return this.compression;
    }
}
