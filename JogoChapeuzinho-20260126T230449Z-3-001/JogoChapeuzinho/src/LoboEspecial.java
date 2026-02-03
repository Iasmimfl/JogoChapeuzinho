import java.awt.Rectangle;

public class LoboEspecial {
    public int x, y;
    private int targetX, targetY;
    private int speed = 2;
    private boolean hasTarget = false;
    public String tipo; // "marrom" ou "preto"

    public LoboEspecial(int x, int y, String tipo) {
        this.x = x;
        this.y = y;
        this.tipo = tipo;
    }

    public void setTarget(int tx, int ty) {
        this.targetX = tx;
        this.targetY = ty;
        this.hasTarget = true;
    }

    public boolean hasTarget() {
        return hasTarget;
    }

    public void moveStep() {
        if (!hasTarget) return;

        int dx = targetX - x;
        int dy = targetY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist < speed) {
            x = targetX;
            y = targetY;
            hasTarget = false;
        } else {
            x += (int) ((dx / dist) * speed);
            y += (int) ((dy / dist) * speed);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 64, 64);
    }
}
