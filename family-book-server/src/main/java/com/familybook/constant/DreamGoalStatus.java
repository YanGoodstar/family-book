package com.familybook.constant;

public final class DreamGoalStatus {

    public static final int ACTIVE = 1;
    public static final int ARCHIVED_COMPLETED = 2;
    public static final int ARCHIVED_STOPPED = 3;

    private DreamGoalStatus() {
    }

    public static int normalize(Integer goalStatus) {
        return goalStatus != null ? goalStatus : ACTIVE;
    }

    public static boolean isArchived(Integer goalStatus) {
        int normalizedStatus = normalize(goalStatus);
        return normalizedStatus == ARCHIVED_COMPLETED || normalizedStatus == ARCHIVED_STOPPED;
    }
}
