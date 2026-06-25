package com.ecofocus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "world_objects")
@Getter @Setter @NoArgsConstructor
public class WorldObject {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "world_id", nullable = false)
    private World world;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(name = "tile_x")
    private Integer tileX;

    @Column(name = "tile_y")
    private Integer tileY;

    public static WOBuilder builder() { return new WOBuilder(); }
    public static class WOBuilder {
        private World world; private String type; private Integer tileX, tileY;
        public WOBuilder world(World v) { this.world = v; return this; }
        public WOBuilder type(String v) { this.type = v; return this; }
        public WOBuilder tileX(Integer v) { this.tileX = v; return this; }
        public WOBuilder tileY(Integer v) { this.tileY = v; return this; }
        public WorldObject build() {
            WorldObject o = new WorldObject();
            o.world = world; o.type = type; o.tileX = tileX; o.tileY = tileY; return o;
        }
    }
}
