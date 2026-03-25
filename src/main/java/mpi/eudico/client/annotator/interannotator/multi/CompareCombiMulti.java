package mpi.eudico.client.annotator.interannotator.multi;

import mpi.eudico.client.annotator.interannotator.CompareCombi;
import mpi.eudico.client.annotator.interannotator.CompareUnit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * A compare combination for more than two raters (i.e. for more than two tiers). It also supports storing a list of values
 * available to the raters e.g. from a controlled vocabulary.
 *
 * @author Han Sloetjes
 * @version Aug 2020
 */
public class CompareCombiMulti extends CompareCombi {
    private List<CompareUnit> unitList;
    private String cvName;
    private Set<String> valuesSet = null;

    /**
     * Constructor for two compare units.
     *
     * @param firstUnit the first compare unit
     * @param secondUnit the second compare unit
     */
    public CompareCombiMulti(CompareUnit firstUnit, CompareUnit secondUnit) {
        super(firstUnit, secondUnit);
        addCompareUnit(firstUnit);
        addCompareUnit(secondUnit);
    }

    /**
     * Constructor, creates a list for compare units.
     */
    public CompareCombiMulti() {
        super(null, null);
        unitList = new ArrayList<CompareUnit>();
    }

    /**
     * Constructor initializing with one compare unit.
     *
     * @param firstUnit the first compare unit
     */
    public CompareCombiMulti(CompareUnit firstUnit) {
        super(firstUnit, null);
        addCompareUnit(firstUnit);
    }

    /**
     * Constructor for any number of compare units.
     *
     * @param compareUnits the compare units to add to the list
     */
    public CompareCombiMulti(CompareUnit... compareUnits) {
        super(null, null);
        for (CompareUnit cu : compareUnits) {
            addCompareUnit(cu);
        }
    }

    /**
     * Constructor with a list of compare units.
     *
     * @param unitList a list containing compare units, the passed list is not copied but becomes the list of this
     *     combination
     */
    public CompareCombiMulti(List<CompareUnit> unitList) {
        super(null, null);
        this.unitList = unitList;

    }

    /**
     * Adds a compare unit to the list.
     *
     * @param compareUnit the compare unit to add
     */
    public void addCompareUnit(CompareUnit compareUnit) {
        if (unitList == null) {
            unitList = new ArrayList<CompareUnit>();
        }
        unitList.add(compareUnit);
    }

    /**
     * @return the first compare unit of the list
     */
    @Override
    public CompareUnit getFirstUnit() {
        if (unitList.size() > 0) {
            return unitList.get(0);
        }

        return null;
    }

    /**
     * @return the second compare unit of the list
     */
    @Override
    public CompareUnit getSecondUnit() {
        if (unitList.size() > 1) {
            return unitList.get(1);
        }

        return null;
    }

    /**
     * Returns the n-th compare unit from the list.
     *
     * @param index the index of the unit to return
     *
     * @return the compare unit at that index or {@code null} if there is no unit at that index
     */
    public CompareUnit getCompareUnit(int index) {
        if (unitList.size() > index && index >= 0) {
            return unitList.get(index);
        }

        return null;
    }

    /**
     * Returns the list of compare units.
     *
     * @return the list of units (not a copy)
     */
    public List<CompareUnit> getCompareUnits() {
        return unitList;
    }

    /**
     * Adds a list of values shared by the compare units, e.g. taken from a shared controlled vocabulary.
     *
     * @param values the values to add to the set of values
     */
    public void addValues(List<String> values) {
        if (valuesSet == null) {
            valuesSet = new HashSet<String>();
        }
        valuesSet.addAll(values);
    }

    /**
     * Adds a single value to the set of values.
     *
     * @param value the value to add
     */
    public void addValue(String value) {
        if (valuesSet == null) {
            valuesSet = new HashSet<String>();
        }
        valuesSet.add(value);
    }

    /**
     * Returns the set of values (or categories).
     *
     * @return the set of values
     */
    public Set<String> getValues() {
        return valuesSet;
    }

    /**
     * Returns the name of a controlled vocabulary the compare units are associated with.
     *
     * @return the name of a controlled vocabulary or {@code null}
     */
    public String getCVName() {
        return cvName;
    }

    /**
     * Sets the name of the associated controlled vocabulary or adds it to an already existing and different name.
     *
     * @param cvName the name of the controlled vocabulary
     */
    public void setCVName(String cvName) {
        if (this.cvName != null && !this.cvName.equals(cvName)) {
            this.cvName = this.cvName + ", " + cvName;
        } else {
            this.cvName = cvName;
        }
    }

}
