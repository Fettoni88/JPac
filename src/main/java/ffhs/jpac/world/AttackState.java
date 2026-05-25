package ffhs.jpac.world;

public class AttackState implements NPCState {

    @Override
    public void update(NPC npc, World world, double deltaTime) {

        npc.stopMoving();

        // Debug (später Animation / Damage)
        System.out.println("NPC greift an!");
    }
}