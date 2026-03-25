package mpi.eudico.client.annotator.imports.multiplefiles;

/**
 * This enum enlists the possible answers to the question asking for approval or permission.
 */
public enum ApprovalEnum {
    YES(1),
    NO(2),
    YES_TO_ALL(0),
    NO_TO_ALL(3);

    private final int indicator;

    private ApprovalEnum(int indicator) {
        this.indicator = indicator;
    }

    /**
     * Convertor to transform the numeric representation of approval choice to enum variable.
     *
     * @param indicator is a numeric representation of approval choice. Code corresponding to choice.
     *
     * @return the enum variable corresponding to the indicator.
     */
    public static ApprovalEnum fromInt(int indicator) {
        for (ApprovalEnum approvalEnum : ApprovalEnum.values()) {
            if (approvalEnum.indicator == indicator) {
                return approvalEnum;
            }

        }

        throw new IllegalArgumentException("Invalid number presented for the option. [%d]".formatted(indicator));
    }
}
