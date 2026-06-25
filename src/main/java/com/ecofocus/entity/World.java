package com.ecofocus.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "worlds")
@Getter @Setter @NoArgsConstructor
public class World {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "map_width")
    private Integer mapWidth = 5;

    @Column(name = "map_height")
    private Integer mapHeight = 5;

    @Column(name = "total_points")
    private Integer totalPoints = 0;

    @Column(name = "total_earned")
    private Integer totalEarned = 0;

    @OneToMany(mappedBy = "world", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorldObject> objects = new ArrayList<>();

    public void addPoints(int pts) {
        this.totalPoints = (this.totalPoints == null ? 0 : this.totalPoints) + pts;
        this.totalEarned = (this.totalEarned == null ? 0 : this.totalEarned) + pts;
        int e = this.totalEarned;
        int size;
        if      (e >= 54000) size = 21;
        else if (e >= 36000) size = 19;
        else if (e >= 24000) size = 17;
        else if (e >= 17280) size = 13;
        else if (e >= 8640)  size = 11;
        else if (e >= 2160)  size = 9;
        else if (e >= 540)   size = 7;
        else                 size = 5;
        this.mapWidth  = size;
        this.mapHeight = size;
    }

    public static WorldBuilder builder() { return new WorldBuilder(); }
    public static class WorldBuilder {
        private User user;
        public WorldBuilder user(User u) { this.user = u; return this; }
        public World build() { World w = new World(); w.user = user; return w; }
    }
}
