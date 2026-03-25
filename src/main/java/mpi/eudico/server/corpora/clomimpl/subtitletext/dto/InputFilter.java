package mpi.eudico.server.corpora.clomimpl.subtitletext.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record InputFilter(
    List<String> selectedForExportTierNames,
    long beginTime,
    long endTime,
    int minimalDuration,
    long offset) {

    public static InputFilter withTier(InputFilter inputFilter, String tierName) {
        return new InputFilter(
            new ArrayList<>(Collections.singletonList(tierName)),
            inputFilter.beginTime,
            inputFilter.endTime,
            inputFilter.minimalDuration,
            inputFilter.offset
        );
    }
}
