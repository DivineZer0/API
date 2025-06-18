package plantime.ru.API.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "list_services")
public class ListServices {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_list_services")
    private Integer idListServices;

    @Column(name = "id_task")
    private Integer idTask;

    @Column(name = "id_service")
    private Integer idService;

    @Column(name = "count")
    private Integer count;

    // getters and setters

    public Integer getIdListServices() { return idListServices; }
    public void setIdListServices(Integer idListServices) { this.idListServices = idListServices; }

    public Integer getIdTask() { return idTask; }
    public void setIdTask(Integer idTask) { this.idTask = idTask; }

    public Integer getIdService() { return idService; }
    public void setIdService(Integer idService) { this.idService = idService; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}