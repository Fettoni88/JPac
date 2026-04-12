package ch.fettoni.rpg.world;

public class ChaseState implements NPCState {

    @Override
    public void update(NPC npc, World world, double deltaTime) {

        npc.chasePlayer(world);
        npc.move(world, deltaTime);
    }
}