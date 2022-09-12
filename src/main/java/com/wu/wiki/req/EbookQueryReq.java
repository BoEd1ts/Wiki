package com.wu.wiki.req;

public class EbookQueryReq extends PageReq{
    private Long id;
    private String name;

    private Long catrgory2Id;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCatrgory2Id() {
        return catrgory2Id;
    }

    public void setCatrgory2Id(Long catrgory2Id) {
        this.catrgory2Id = catrgory2Id;
    }

    @Override
    public String toString() {
        return "EbookQueryReq{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", catrgory2Id=" + catrgory2Id +
                ", description='" + description + '\'' +
                "} " + super.toString();
    }
}