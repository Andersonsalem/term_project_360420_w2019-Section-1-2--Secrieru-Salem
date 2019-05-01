import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.*;
import java.lang.Math.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.Timer;
 
public class Boids extends JPanel {
    Flock flock;
    public int w;
	public int h;
 
    public Boids() {
         w = 800;
         h = 600;
 
        setPreferredSize(new Dimension(w, h));
        setBackground(Color.white);
 
        apparition();
 
        new Timer(17, (ActionEvent e) -> {
            if (flock.hasLeftTheBuilding(w))
                apparition();
            repaint();
        }).start();
    }
 
    private void apparition() {
        flock = Flock.spawn(-300, h * 0.5, 250);
    }
 
    @Override
    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
 
        flock.run(g, w, h);
    }
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setTitle("Boids");
            f.setResizable(false);
            f.add(new Boids(), BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
 
class Boid {
    static final Random r = new Random();
    static final Vec migrate = new Vec(0.01, 0.0);
    static final int size = 1;
    static final Path2D shape = new Path2D.Double();
 
    static {
        shape.moveTo(0, -size * 2);
        shape.lineTo(-size, size * 2);
        shape.lineTo(size, size * 2);
        shape.closePath();
    }
 
    final double maxForce, maxSpeed;
 
    Vec location, velocity, acceleration;
    boolean included = true;
 
    Boid(double x, double y) {
        acceleration = new Vec();
        velocity = new Vec(r.nextInt(3) + 1, r.nextInt(3) - 1);
        location = new Vec(x, y);
        maxSpeed = 4.0;
        maxForce = 0.05;
    }
 
    void update() {
        velocity.add(acceleration);
        velocity.limit(maxSpeed);
        location.add(velocity);
        acceleration.mult(0);
    }
 
    void applyForce(Vec force) {
        acceleration.add(force);
    }
 
    Vec seek(Vec target) {
        Vec steer = Vec.sub(target, location);
        steer.normalize();
        steer.mult(maxSpeed);
        steer.sub(velocity);
        steer.limit(maxForce);
        return steer;
    }
 
    void flock(Graphics2D g, List<Boid> boids) {
        view(g, boids);
 
        Vec rule1 = separation(boids);
        Vec rule2 = alignment(boids);
        Vec rule3 = cohesion(boids);
 
        rule1.mult(2.5);
        rule2.mult(1.5);
        rule3.mult(1.3);
 
        applyForce(rule1);
        applyForce(rule2);
        applyForce(rule3);
        applyForce(migrate);
    }
 
    void view(Graphics2D g, List<Boid> boids) {
        double sightDistance = 70;
        double peripheryAngle = PI * 0.85;
 
        for (Boid b : boids) {
            b.included = false;
 
            if (b == this)
                continue;
 
            double d = Vec.dist(location, b.location);
            if (d <= 0 || d > sightDistance)
                continue;
 
            Vec lineOfSight = Vec.sub(b.location, location);
 
            double angle = Vec.angleBetween(lineOfSight, velocity);
            if (angle < peripheryAngle)
                b.included = true;
        }
    }
 
    Vec separation(List<Boid> boids) {
        double desiredSeparation = 15;
 
        Vec steer = new Vec(0, 0);
        int count = 0;
        for (Boid b : boids) {
            if (!b.included)
                continue;
 
            double d = Vec.dist(location, b.location);
            if ((d > 0) && (d < desiredSeparation)) {
                Vec diff = Vec.sub(location, b.location);
                diff.normalize();
                diff.div(d);        // weight by distance
                steer.add(diff);
                count++;
            }
        }
        if (count > 0) {
            steer.div(count);
        }
 
        if (steer.mag() > 0) {
            steer.normalize();
            steer.mult(maxSpeed);
            steer.sub(velocity);
            steer.limit(maxForce);
            return steer;
        }
        return new Vec(0, 0);
    }
 
    Vec alignment(List<Boid> boids) {
        double preferredDist = 100;
 
        Vec steer = new Vec(0, 0);
        int count = 0;
 
        for (Boid b : boids) {
            if (!b.included)
                continue;
 
            double d = Vec.dist(location, b.location);
            if ((d > 0) && (d < preferredDist)) {
                steer.add(b.velocity);
                count++;
            }
        }
 
        if (count > 0) {
            steer.div(count);
            steer.normalize();
            steer.mult(maxSpeed);
            steer.sub(velocity);
            steer.limit(maxForce);
        }
        return steer;
    }
 
    Vec cohesion(List<Boid> boids) {
        double preferredDist = 50;
 
        Vec target = new Vec(0, 0);
        int count = 0;
 
        for (Boid b : boids) {
            if (!b.included)
                continue;
 
            double d = Vec.dist(location, b.location);
            if ((d > 0) && (d < preferredDist)) {
                target.add(b.location);
                count++;
            }
        }
        if (count > 0) {
            target.div(count);
            return seek(target);
        }
        return target;
    }
 
    void draw(Graphics2D g) {
        AffineTransform save = g.getTransform();
 
        g.translate(location.x, location.y);
        g.rotate(velocity.heading() + PI / 2);
        g.setColor(Color.white);
        g.fill(shape);
        g.setColor(Color.black);
        g.draw(shape);
 
        g.setTransform(save);
    }
 
    void run(Graphics2D g, List<Boid> boids, int w, int h) {
        flock(g, boids);
        update();
        draw(g);
    }
}
 
class Flock {
    List<Boid> boids;
 
    Flock() {
        boids = new ArrayList<>();
    }
 
    void run(Graphics2D g,  int w, int h) {
        for (Boid b : boids) {
            b.run(g, boids, w, h);
        }
    }
 
    boolean hasLeftTheBuilding(int w) {
        int count = 0;
        for (Boid b : boids) {
            if (b.location.x + Boid.size > w)
                count++;
        }
        return boids.size() == count;
    }
 
    void addBoid(Boid b) {
        boids.add(b);
    }
 
    static Flock spawn(double w, double h, int numBoids) {
        Flock flock = new Flock();
        for (int i = 0; i < numBoids; i++)
            flock.addBoid(new Boid(w, h));
        return flock;
    }
}
 
class Vec {
    double x, y;
 
    Vec() {
    }
 
    Vec(double x, double y) {
        this.x = x;
        this.y = y;
    }
 
    void add(Vec v) {
        x += v.x;
        y += v.y;
    }
 
    void sub(Vec v) {
        x -= v.x;
        y -= v.y;
    }
 
    void div(double val) {
        x /= val;
        y /= val;
    }
 
    void mult(double val) {
        x *= val;
        y *= val;
    }
 
    double mag() {
        return sqrt(pow(x, 2) + pow(y, 2));
    }
 
    double dot(Vec v) {
        return x * v.x + y * v.y;
    }
 
    void normalize() {
        double mag = mag();
        if (mag != 0) {
            x = x / mag;
            y = y / mag;
        }
    }
 
    void limit(double lim) {
        double mag = mag();
        if (mag != 0 && mag > lim) {
            x = x * (lim / mag);
            y = y * (lim / mag);
        }
    }
 
    double heading() {
        return atan2(y, x);
    }
 
    static Vec sub(Vec v, Vec v2) {
        return new Vec(v.x - v2.x, v.y - v2.y);
    }
 
    static double dist(Vec v, Vec v2) {
        return sqrt(pow(v.x - v2.x, 2) + pow(v.y - v2.y, 2));
    }
 
    static double angleBetween(Vec v, Vec v2) {
        return acos(v.dot(v2) / (v.mag() * v2.mag()));
    }
}
