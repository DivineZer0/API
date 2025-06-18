package plantime.ru.API.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import plantime.ru.API.entity.ListOfSoftware;
import plantime.ru.API.dto.ListOfSoftwareDTO;
import plantime.ru.API.repository.ListOfSoftwareRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ListOfSoftwareService {
    @Autowired
    private ListOfSoftwareRepository repo;

    public List<ListOfSoftwareDTO> findAll() {
        return repo.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }
    public List<ListOfSoftwareDTO> findByProject(Integer idProject) {
        return repo.findByIdProject(idProject).stream().map(this::toDTO).collect(Collectors.toList());
    }
    public ListOfSoftwareDTO save(ListOfSoftwareDTO dto) {
        ListOfSoftware saved = repo.save(fromDTO(dto));
        return toDTO(saved);
    }
    public void delete(Integer id) { repo.deleteById(id); }

    public ListOfSoftwareDTO toDTO(ListOfSoftware e) {
        return new ListOfSoftwareDTO(e.getIdListOfSoftware(), e.getIdProject(), e.getIdSoftware());
    }
    public ListOfSoftware fromDTO(ListOfSoftwareDTO dto) {
        return new ListOfSoftware(dto.getIdListOfSoftware(), dto.getIdProject(), dto.getIdSoftware());
    }
}