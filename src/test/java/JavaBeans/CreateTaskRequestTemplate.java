package JavaBeans;

public class CreateTaskRequestTemplate {
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getProject_id() {
        return project_id;
    }

    public void setProject_id(Long project_id) {
        this.project_id = project_id;
    }

    public Long getSection_id() {
        return section_id;
    }

    public void setSection_id(Long section_id) {
        this.section_id = section_id;
    }

    public Long getParent_id() {
        return parent_id;
    }

    public void setParent_id(Long parent_id) {
        this.parent_id = parent_id;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Integer[] getLabel_ids() {
        return label_ids;
    }

    public void setLabel_ids(Integer[] label_ids) {
        this.label_ids = label_ids;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getDue_string() {
        return due_string;
    }

    public void setDue_string(String due_string) {
        this.due_string = due_string;
    }

    public String getDue_date() {
        return due_date;
    }

    public void setDue_date(String due_date) {
        this.due_date = due_date;
    }

    public String getDue_datetime() {
        return due_datetime;
    }

    public void setDue_datetime(String due_datetime) {
        this.due_datetime = due_datetime;
    }

    public String getDue_lang() {
        return due_lang;
    }

    public void setDue_lang(String due_lang) {
        this.due_lang = due_lang;
    }

    public Integer getAssignee() {
        return assignee;
    }

    public void setAssignee(Integer assignee) {
        this.assignee = assignee;
    }

    private String content;
    private String description;
    private Long project_id;
    private Long section_id;
    private Long parent_id;
    private Integer order;
    private Integer[] label_ids;
    private Integer priority;
    private String due_string;
    private String due_date;
    private String due_datetime;
    private String due_lang;
    private Integer assignee;


}
