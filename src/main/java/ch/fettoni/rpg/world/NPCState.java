package ch.fettoni.rpg.world;

public interface NPCState {
    void update(NPC npc, World world, double deltaTime);
}