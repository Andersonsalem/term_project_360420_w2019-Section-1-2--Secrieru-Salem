import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.*;
import static java.lang.Math.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.Timer;
 
 
 //////////////////////////////////////////////////////////////////////////////////////////////
 /*Notes to Dragos : 
 IMPORTANT EVERYTHING THAT IS MEANT TO YOU MUST BE ERASED BEFORE BEING SUBMITTED
 Every time you see a math functions without math before it it's because one 
 of the imported packages contains the class for the function so no need
 to write them as Math.(something)
 atan2(double x, double y) converts stuff to polar coordinates)
 Math.acos is obvious
 */
 //////////////////////////////////////////////////////////////////////////////////////////////


//Boids is a child class to the parent class Jframe 
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
            if (flock.outofscreen(w, h))
                apparition(); 
			/* discarded experiment
			apparition (w, h);
			*/
            repaint();
        }).start();
    }
 //method for the apparition of the boids ( x position, y position and number of boids) 
    private void apparition() {
        flock = Flock.spawn(-300, h * 0.5, 30);
    }
	/* dicarded experiment
	private void apparition(double x, double y) {
		flock = Flock.spawn(-x, -y, 30);
	}*/
 
 
 // overrides the child class above
    
    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
 
        flock.run(g, w, h);
    }
 // Parameters of the box
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
 // the class that define each boid
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
	// setup variables
    final double maxForce;
	final double maxSpeed;
 
    Vec location;
	Vec velocity;
	Vec acceleration;
    boolean included = true;


 // for each boid you give a position on the plane which is modified according to velocity and acceleration
    Boid(double x, double y) {
        acceleration = new Vec();
        velocity = new Vec(r.nextInt(3) + 1, r.nextInt(3) - 1);
        location = new Vec(x, y);
        maxSpeed = 4.0;
        maxForce = 0.05;
    }
 //updates the position
    void update() {
        velocity.add(acceleration);
        velocity.limit(maxSpeed);
        location.add(velocity);
        acceleration.mult(0);
    }
 //method in order to make the forces affect the boid pposition
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
 
		// Defining each rule
        Vec rule1 = separation(boids);
        Vec rule2 = alignment(boids);
        Vec rule3 = cohesion(boids);
		
		
		// the degree at which each rule is applied
        rule1.mult(2.5);
        rule2.mult(0.7);
        rule3.mult(0.5);
 
		//applying the forces according to the magnitude of each rule
        applyForce(rule1);
        applyForce(rule2);
        applyForce(rule3);
        applyForce(migrate);
    }
	// Dragos ask DingYi or Zach for that one
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
  // parameters of the separation rule
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
 // used to draw the shape of the boids
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
 // run the animation
    void run(Graphics2D g, List<Boid> boids, int w, int h) {
        flock(g, boids);
        update();
        draw(g);
    }
}
// defines the flock as whole 
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
 //defines what it means to be out of screen
    boolean outofscreen(int w, int h) {
        int count = 0;
        for (Boid b : boids) {
            if ((b.location.x + Boid.size > w) || ((abs(b.location.y + Boid.size))> h))
                count++;
        }
        return boids.size() == count;
    }
 // adds a boid to the list
    void addBoid(Boid b) {
        boids.add(b);
    }
 // uses the add boid method in order to have as many as the number input
    static Flock spawn(double w, double h, int numBoids) {
        Flock flock = new Flock();
        for (int i = 0; i < numBoids; i++)
            flock.addBoid(new Boid(w, h));
        return flock;
    }
}

 // most important class of the whole project : litteraly the backbone because it defines all position changes
class Vec {
    double x, y;
 
    Vec() {
    }
 
    Vec(double x, double y) {
        this.x = x;
        this.y = y;
    }
/////////////////////////////////////////////////////////////	
 //defining operation using vector coordinates ( ie u have (x, y)and u move a bit then you add this bit (x, Y) 
    void add(Vec v) {
        x = x + v.x;
        y = y + v.y;
    }
 
    void sub(Vec v) {
        x = x - v.x;
        y = x - v.y;
    }
 
    void div(double val) {
        x = x / val;
        y = y / val;
    }
 
    void mult(double val) {
        x = x * val;
        y = y * val;
    }
 //////////////////////////////////////////////////////////
 // magnitude of a vector
    double mag() {
        return sqrt( x * x + y * y);
    }
 // dot product
    double dot(Vec v) {
        return x * v.x + y * v.y;
    }
 //normalize the vector
    void normalize() {
        double mag = mag();
        if (mag != 0) {
            x = x / mag;
            y = y / mag;
        }
    }
 //creates a vector whose magnitude is that of lim
    void limit(double lim) {
        double mag = mag();
        if (mag != 0 && mag > lim) {
            x = x * (lim / mag);
            y = y * (lim / mag);
        }
    }
 //converts coordinates to polar ones
    double heading() {
        return atan2(y, x);
    }
 // substraction of 2 vectors
    static Vec sub(Vec v, Vec v2) {
        return new Vec(v.x - v2.x, v.y - v2.y);
    }
 //i dont remember and im tired
    static double dist(Vec v, Vec v2) {
        return sqrt((v.x - v2.x) * (v.x - v2.x) + (v.y - v2.y) * (v.y - v2.y));
    }
 //self explanatory
    static double angleBetween(Vec v, Vec v2) {
        return acos(v.dot(v2) / (v.mag() * v2.mag()));
    }
}
