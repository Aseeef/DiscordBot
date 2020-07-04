package xenforo.objects.tickets;

public enum Department {

    BUY_AN_UNBAN(6, "Buy an Unban"),
    PUNISHMENT_APPEALS(2, "Punishment Appeals"),
    PURCHASES(1, "Purchases"),
    PLAYER_REPORTS(5, "Player Reports"),
    STAFF_REPORTS(8, "Staff Reports"),
    OTHER_SUPPORT(9, "Other Support");

    private int departmentId;
    private String departmentName;

    Department(int departmentId, String departmentName) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public static Department getDepartment(int departmentId) {
        for (Department d : Department.values()) {
            if (d.getDepartmentId() == departmentId)
                return d;
        }
        return null;
    }

}
