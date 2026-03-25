package mpi.eudico.server.corpora.clomimpl.html;

/**
 * Data structure for the export TimeAlignedInterlinear. Contains a tier and their preferences.
 *
 * @author Steffen Zimmermann
 * @version 1.0
 */
public class TAITierSetting {

    private String tierName;
    private boolean underlined;
    private boolean bold;
    private boolean italic;
    private boolean reference = false;

    /**
     * Creates a new tier settings instance.
     * 
     * @param tierName the name of the tier
     * @param underlined whether the text is underlined or not
     * @param bold whether the text is in bold style
     * @param italic whether the text is in italic style
     */
    public TAITierSetting (String tierName, boolean underlined, boolean bold, boolean italic) {
        this.tierName = tierName;
        this.underlined = underlined;
        this.italic = italic;
        this.bold = bold;
    }

    /**
     * Returns the tier name.
     * 
     * @return the tier name
     */
    public String getTierName() {
        return tierName;
    }

    /**
     * Sets the tier name.
     * 
     * @param tierName the tier name
     */
    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    /**
     * Returns whether the text of this tier is underlined.
     * 
     * @return {@code true} if the text is underlined
     */
    public boolean isUnderlined() {
        return underlined;
    }

    /**
     * Sets whether the text for this tier should be underlined.
     * 
     * @param underlined if {@code true} the text of this tier will be 
     * underlined in the output
     */
    public void setUnderlined(boolean underlined) {
        this.underlined = underlined;
    }

    /**
     * Returns whether the text of this tier is styled in bold.
     * 
     * @return {@code true} if the text of this tier is in bold style
     */
    public boolean isBold() {
        return bold;
    }

    /**
     * Sets whether the text of this tier should be in bold style.
     * 
     * @param bold if {@code true} the text will be in bold style 
     */
    public void setBold(boolean bold) {
        this.bold = bold;
    }

    /**
     * Returns whether the text of this tier is in italic style.
     * 
     * @return {@code true} if the text is in italic style, {@code false}
     * otherwise
     */
    public boolean isItalic() {
        return italic;
    }

    /**
     * Sets whether the text of this tier should be in italic style.
     * 
     * @param italic if {@code true} the text will be in italic style
     */
    public void setItalic(boolean italic) {
        this.italic = italic;
    }

    /**
     * Returns whether this tier is set as the reference tier.
     * 
     * @return {@code true} if this tier is the reference tier
     */
    public boolean isReference() {
        return reference;
    }

    /**
     * Sets whether this tier is selected as the reference tier.
     * 
     * @param reference if {@code true} this tier is the reference tier in the
     * output
     */
    public void setReference(boolean reference) {
        this.reference = reference;
    }

}
