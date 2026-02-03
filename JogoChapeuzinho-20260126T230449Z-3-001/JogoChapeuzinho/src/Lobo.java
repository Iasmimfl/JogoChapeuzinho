public class Lobo {
    // posição em pixels (inteiros para compatibilidade)
    public int x, y;
    private int speed = 2;
    // destino em pixels
    private Integer targetX = null, targetY = null;

    public Lobo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // compatibilidade com constructor antigo
    public Lobo(int x, int y, int direcao, int limiteEsq, int limiteDir) {
        this(x, y);
    }

    public boolean hasTarget() {
        return targetX != null && targetY != null;
    }

    public void setTarget(int tx, int ty) {
        this.targetX = tx;
        this.targetY = ty;
    }

    public void clearTarget() {
        this.targetX = null;
        this.targetY = null;
    }

    // move um passo em direção ao target; retorna true se chegou
    public boolean moveStep() {
        if (!hasTarget()) return true;
        int dx = targetX - x;
        int dy = targetY - y;
        if (Math.abs(dx) <= speed && Math.abs(dy) <= speed) {
            x = targetX; y = targetY;
            clearTarget();
            return true;
        }
        double ang = Math.atan2(dy, dx);
        x += (int)Math.round(Math.cos(ang) * speed);
        y += (int)Math.round(Math.sin(ang) * speed);
        return false;
    }

    public java.awt.Rectangle getBounds() {
        return new java.awt.Rectangle(x, y, 64, 64);
    }
}