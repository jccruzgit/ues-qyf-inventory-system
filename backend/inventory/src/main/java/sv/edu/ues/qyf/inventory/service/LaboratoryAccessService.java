package sv.edu.ues.qyf.inventory.service;

import java.util.List;

public interface LaboratoryAccessService {

    void validateAccessToLaboratory(Long laboratoryId);

    boolean hasAccessToAllLaboratories();

    List<Long> getAccessibleLaboratoryIds();
}
