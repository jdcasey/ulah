package org.commonjava.ulah.launch;

public class BootStatus {

    private Throwable error;

    private int exit;

    public BootStatus() {
        exit = -1;
    }

    public BootStatus(final int exit, final Throwable error) {
        markFailed(exit, error);
    }

    public void markFailed(final int exit, final Throwable error) {
        this.exit = exit;
        this.error = error;
    }

    public boolean isSet() {
        return exit > -1;
    }

    public boolean isFailed() {
        return exit > 0;
    }

    public boolean isSuccess() {
        return exit == 0;
    }

    public Throwable getError() {
        return error;
    }

    public int getExitCode() {
        return exit;
    }

    public void markSuccess() {
        exit = 0;
    }

}
