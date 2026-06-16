package model;

import java.io.Serializable;

/**
 * Model class representing 'room_types' configuration catalog.
 */
public class RoomType implements Serializable {
    private static final long serialVersionUID = 1L;

    private int typeId;
    private String typeName;

    // Constructors
    public RoomType() {}

    public RoomType(int typeId, String typeName) {
        this.typeId = typeId;
        this.typeName = typeName;
    }

    public RoomType(String typeName) {
        this.typeName = typeName;
    }

    // Getters and Setters
    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return "RoomType{" +
                "typeId=" + typeId +
                ", typeName='" + typeName + '\'' +
                '}';
    }
}
