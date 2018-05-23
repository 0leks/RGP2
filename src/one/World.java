package one;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;

import gui.Constants;
import gui.Frame;

public class World implements Serializable {
  
  // SETTINGS
	public static final int THREE_D_RATIO = 8;
	public static final boolean DRAWPLAYEROBSTACLES = true;
	public static boolean NO_COLLISION = false;
  public static int ZOOM = 1;
	

	// default settings
  public boolean drawimage = true;
  public boolean draw3d = true;
  public static boolean playmusic = false;
  

  public static final int CANTMOVE = -1;
  public static final int CANMOVE = -2;
  
	private Queue<Mob> mobs;
  private Queue<Projectile> projectiles;
  private Queue<ProjectileRegion> projectileRegions;
	private Queue<Obstacle> walls;
	private Queue<Shop> shops;
	private ArrayList<Spawn> spawns;
	private ArrayList<Message> messages;
	private Queue<Sign> signs;
	private ArrayList<Race> races;
	private ArrayList<SoundArea> sounds;
	
	public Player playerASDF;
	private Mob snitch;
	
	protected int deathTransparency;
	private Random rand;
	private Point mouse;
	public int selected = 0;
	
	Sound grass;
	Sound arena;
	Sound training;
	Sound death;
	Sound mountain;
	Sound boss1;
	Sound barrack;
	Sound lava;
  Sound necro;
  Sound empty;
	Sound currentsound;
	
	public World() {
		Frame.println("Initializing World");
		mouse = new Point(0, 0);
		mobs = new ConcurrentLinkedQueue<Mob>();
		projectiles = new ConcurrentLinkedQueue<Projectile>();
		projectileRegions = new ConcurrentLinkedQueue<ProjectileRegion>();
		walls = new ConcurrentLinkedQueue<Obstacle>();
		shops = new ConcurrentLinkedQueue <Shop>();
		spawns = new ArrayList<Spawn>();
		messages = new ArrayList<Message>();
		signs = new ConcurrentLinkedQueue<Sign>();
		races = new ArrayList<Race>();
		sounds = new ArrayList<SoundArea>();
		rand = new Random();

		initializeSounds();
		initializeworld();
		//changeSound(null);
	}
	
	public void initializeSounds() {
		Frame.print("Loading World Music: ");
		death = initSound("death.wav", -2, false);
		grass = initSound("grass.wav", -15, true);
		sounds.add(new SoundArea(grass) {
			@Override
			public boolean in(int x, int y) {
				if(x>-50 && x<1150 && y>-250 && y<1000) {
					return true;
				}
				return false;
			}
		});
		arena = initSound("combat1.wav", -15, true);
		sounds.add(new SoundArea(arena) {
			@Override
			public boolean in(int x, int y) {
				if(x>-1200 && x<-70 && y>100 && y<1000) {
					return true;
				}
				return false;
			}
		});
		training = initSound("training.wav", -7, false);
		sounds.add(new SoundArea(training) {
			@Override
			public boolean in(int x, int y) {
				if(x>1400 && x<1750 && y>90 && y<1000) {
					return true;
				}
				return false;
			}
		});
		mountain = initSound("stronghold.wav", -16, true);
		sounds.add(new SoundArea(mountain) {
			@Override
			public boolean in(int x, int y) {
				if(x>-1100 && x<-50 && y>-1000 && y<100) {
					return true;
				}
				return false;
			}
		});
		boss1 = initSound("combat4.wav", -5, false);
		sounds.add(new SoundArea(boss1) {
			@Override
			public boolean in(int x, int y) {
				if(x>350 && x<1000 && y>-1100 && y<-450) {
					return true;
				}
				return false;
			}
		});
		barrack = initSound("combat3.wav", -5, true);
		sounds.add(new SoundArea(barrack) {
			@Override
			public boolean in(int x, int y) {
				if(x>250 && x<1350 && y<-1400 && y>-2000) {
					return true;
				}
				return false;
			}
		});
		lava = initSound("lava.wav", -5, true);
		sounds.add(new SoundArea(lava) {
			@Override
			public boolean in(int x, int y) {
				if(x>1350 && x<3100 && y<-900 && y>-3200) {
					return true;
				}
				if( x>450 && x < 1350 && y > -3200 && y < -2100 ) { 
				  return true;
				}
				return false;
			}
		});
    necro = initSound("necro.wav", -3, true);
    sounds.add(new SoundArea(necro) {
      @Override
      public boolean in(int x, int y) {
        if(x>-1400 && x<450 && y>-4100 && y<-3000) {
          return true;
        }
        return false;
      }
    });
    empty = initSound("empty.wav", -5, true);
    sounds.add(new SoundArea(empty) {
      @Override
      public boolean in(int x, int y) {
        if(x>-4000 && x<-100 && y>-2700 && y<-1400) {
          return true;
        }
        return false;
      }
    });
		Frame.println();
	}
	public void changeSound(Sound sound) {
		if(currentsound!=sound) {
			if(currentsound!=null) {
				currentsound.fadeOut(.15);
			}
			if(sound!=null) {
				sound.fadeIn(.05);
			}
			currentsound = sound;
			if(currentsound!=null)
				System.out.println("currentsound is now " + currentsound.getUrl());
		} else {
			
		}
	}
	public void stopMusic() {
	  playmusic = false;
	  if( currentsound != null ) {
	    currentsound.fadeOut(10);
	    currentsound.stopplaying();
	    currentsound = null;
	  }
	}
	public void startMusic() {
	  playmusic = true;
	  for(SoundArea s : sounds) {
      if(s.in(playerASDF.x(), playerASDF.y())) {
        changeSound(s.getSound());
      }
    }
	}
	public static Sound initSound(final String url, float volume, boolean loop) {
		Frame.print(url +", ");
		Sound bob = new Sound(url, volume, loop);
		bob.start();
		return bob;
	}
	
	public static Color darken(Color c, int mag) {
	  int randomModifier = 2 - (int)(Math.random()*5);
		int r = c.getRed() - mag + randomModifier;
		int g = c.getGreen() - mag + randomModifier;
		int b = c.getBlue() - mag + randomModifier;
		if(r<0)
			r=0;
		if(g<0)
			g=0;
		if(b<0)
			b=0;
		if(r>250)
			r=250;
		if(g>250)
			g=250;
		if(b>250)
			b=250;
		return new Color(r, g, b);
	}
	public void addMessage(String s, int time) {
		messages.add(new Message(s, time));
	}
	public void newgame(String race) {
	  String weapon = "fist";
//	  weapon = "diamond laser";
		initPlayer(400, 400, weapon, 15, new ArrayList<Item>(), race, 0);
//  initPlayer(-1200, -1900, weapon, 15, new ArrayList<Item>(), race, 0);
//		initPlayer(-300, -3050, weapon, 15, new ArrayList<Item>(), race, 0);
		//TODO INITPLAYER
//		initPlayer(1300, -1500, weapon, 15, new ArrayList<Item>(), race, 0);
//    initPlayer(850, -1350, weapon, 15, new ArrayList<Item>(), race, 0);
	}
	public void initializemob(Mob m, String weapon) {
		Race r = m.race;
		
		m.basedamage = r.startdmg;
		m.damagebuff = 0;
		m.wordamage = 1;
		
		m.level = 1;
		
		m.totalhealthbuff = r.starthealth;
		m.worhealth = 1;
		
		m.woradelay = 1;
		
		m.accel = r.startaccel;
		m.woraccel = 1;
		
		m.agilitybuff = 0;
		m.actagility = r.startagi;
		m.woragility = 1;
		
		m.strengthbuff = 0;
		m.actstrength = r.startstr;
		m.worstrength = 1;
		
		m.intelligencebuff = 0;
		m.actintelligence = r.startint;
		m.worintelligence = 1;
		
		m.regenbuff = r.startregen;
		m.regen = 0;
		m.worregen = 1;
		
		m.basearmor = r.startarmor;
		m.armorbuff = 0;
		m.worarmor = 1;
		
		m.getWeap(weapon);
		m.weapon.puton(m);
		m.health = m.getMaximumHealth();
		
		m.race = r;
		
		m.experience = 0;
		m.exptolvlup += (int) (Math.pow(m.level, 2)*10/m.getIntelligence())+10;
		m.rescale();
	}
	public void initWall(int xx, int yy, int ww, int hh, int re, int gr, int bl, boolean blockplayer) {
		Obstacle w = new Obstacle(xx, yy, ww, hh, this, getColor(re, gr, bl), blockplayer);
		walls.add(w);
	}
	public void initSign(int xx, int yy, int ww, int hh, String imageloc, String message) {
		Sign w = new Sign(xx, yy, ww, hh, this, imageloc, message);
		signs.add(w);
	}
	public Color getColor(int r, int g, int b) {
		if(r<0)
			r=0;
		if(g<0)
			g=0;
		if(b<0)
			b=0;
		if(r>255)
			r=255;
		if(g>255)
			g=255;
		if(b>255)
			b=255;
		return new Color(r, g, b);
	}
	public void addProjectile(Projectile p ) {
	  projectiles.add(p);
	}
  public void removeProjectile(Projectile p ) {
    projectiles.remove(p);
  }
	public Mob initMob(String race, int exp, int money, int xx, int yy, int health, String weap, String ai) {
		Mob m = new Mob(xx, yy, ai, this, getrace(race));
		this.initializemob(m, weap);
		m.experience = exp;
		m.money = money;
		m.lvlup();
		m.health = health;
		if(m.getCurrentHealth() <= 0) {
			m.dead = true;
		}
		mobs.add(m);
		return m;
	}
	public void initPlayer(int xx, int yy, String weap, int smoney, ArrayList<Item> items, String race, int exp, int health) {
		System.out.println(race);
		playerASDF = new Player(xx, yy, this, getrace(race));
		this.initializemob(playerASDF, weap);
		playerASDF.money = smoney;
		for(Item i : items) {
			playerASDF.addItem(i);
		}
		playerASDF.experience = exp;
//		p.lvlupto(40);
		playerASDF.lvlup();
		playerASDF.health = health;
		if(playerASDF.getCurrentHealth()<=0) {
			playerASDF.dead = true;
		}
	}
	public void initPlayer(int xx, int yy, String weap, int smoney, ArrayList<Item> items, String race, int exp) {
		System.out.println(race);
		playerASDF = new Player(xx, yy, this, getrace(race));
		this.initializemob(playerASDF, weap);
		playerASDF.money = smoney;
		for(Item i : items) {
			playerASDF.addItem(i);
		}
		playerASDF.experience = exp;
//  p.lvlupto(40);
		playerASDF.lvlup();
		if(playerASDF.getCurrentHealth()<=0) {
			playerASDF.dead = true;
		}
	}
	public void draw(Graphics2D g) {
		g.setColor(new Color(220, 220, 220));
		g.fillRect(0, 0, 1960, 1080);
	  // Draw the shops
		Iterator<Shop> iterator = shops().iterator();
    while (iterator.hasNext()) {
      Shop shop = iterator.next();
      drawShop(g, shop);
    }

    // Draw the projectiles
    Iterator<Projectile> itproj = projectiles.iterator();
    while( itproj.hasNext() ) {
      Projectile proj = itproj.next();
      drawProjectile(g, proj);
    }
    
    // Draw the Walls
    Iterator<Obstacle> itwall = walls().iterator();
    while( itwall.hasNext() ) {
      Obstacle obst = itwall.next();
      drawObstacle(g, obst);
    }

    // Draw the player
    drawPlayer(g, playerASDF);

    // Draw all the mobs
    Iterator<Mob> itmob = mobs().iterator();
    while( itmob.hasNext() ) {
    	Mob mob = itmob.next();
    	drawMob(g, mob);
    }
		
		// Draw the signs
		Iterator<Sign> itsign = signs.iterator();
		while( itsign.hasNext() ) {
		  Sign sign = itsign.next();
      drawObstacle(g, sign);
		}
		
		g.setColor(Color.black);
		int x = 400;
		int y = 30;
		int size = messages.size();
		for(int a = 0; a<size; a++) {
			Message s = messages.get(a);
			if(s.drawleft-->0) {
				g.drawString(s.string, x, y);
				y+=20;
			} else {
				messages.remove(s);
				size = messages.size();
			}
			
		}
    // Draw the mob popups
		itmob = mobs.iterator();
		while( itmob.hasNext() ) {
		  Mob m = itmob.next();
		  drawPopups(g, m);
		}
    // Draw the Player's popups
		drawPopups(g, playerASDF);
		drawgui(g);
	}
	
	// how far offscreen before start drawing things
	public static final int DRAWCHECK = 50;
	public int MINDRAWX, MINDRAWY, MAXDRAWX, MAXDRAWY;
	
	public void drawPopups( Graphics2D g, Mob mob ) {
	  int drawx = (mob.x-playerASDF.x)/World.ZOOM+Panel.MIDX;
    int drawy = (mob.y-playerASDF.y)/World.ZOOM+Panel.MIDY;
    boolean draw = false;
    if(drawx>MINDRAWX && drawx<MAXDRAWX && drawy>MINDRAWY && drawy<MAXDRAWY) {
      draw = true;
    }
    Iterator<Popup> itpop = mob.popups.iterator();
    while( itpop.hasNext() ) {
      Popup pop = itpop.next();
      g.setColor(pop.color);
      if(draw)
        g.drawString(pop.string, drawx+pop.x(), drawy+pop.y());
      if(pop.drawn()) {
        mob.popups.remove(pop);
      }
    }
	}
	public void drawProjectile( Graphics2D g, Projectile projectile ) {
    g.setColor(Color.blue);
    Color[] colors = new Color[6];
    colors[0] = projectile.getColor();
    for(int a=1; a<colors.length && a<3; a++) {
      colors[a] = World.darken(colors[0], -90);
    }
    for(int a=3; a<colors.length; a++) {
      colors[a] = World.darken(colors[0], 90);
    }
    drawThing(g, projectile, colors, World.THREE_D_RATIO*8);
  }
	public void drawMob( Graphics2D g, Mob mob) {
	  int drawx = (mob.x-playerASDF.x)/World.ZOOM+Panel.MIDX;
    int drawy = (mob.y-playerASDF.y)/World.ZOOM+Panel.MIDY;
    int w = mob.w/World.ZOOM;
    int h = mob.h/World.ZOOM;
    int distx = 0;
    int disty = 0;
    if(draw3d) {
      distx = (drawx-Panel.MIDX)/THREE_D_RATIO/World.ZOOM;
      disty = (drawy-Panel.MIDY)/THREE_D_RATIO/World.ZOOM;
    }
    //g.fill(dim());
    if(mob.attack!=null && mob.adraw>=0) {
      int nx = (mob.attack.x-playerASDF.x)/World.ZOOM+Panel.MIDX;
      int ny = (mob.attack.y-playerASDF.y)/World.ZOOM+Panel.MIDY;
      int nw = mob.attack.width/World.ZOOM;
      int nh = mob.attack.height/World.ZOOM;
      if(mob.attackdirection == 1 || mob.attackdirection==3) {
        nw = mob.attack.height/World.ZOOM;
        nh = mob.attack.width/World.ZOOM;
      }
      if(nx+nw>MINDRAWX && nx-nw<MAXDRAWX && ny+nh>MINDRAWY && ny-nh<MAXDRAWY) {
        Color cur = g.getColor();
        if(mob.isHostile()) {
          g.setColor(Color.red);
        } else {
          g.setColor(Color.green);
        }
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(nx, ny);
        g2d.rotate(Math.toRadians(mob.attackdirection*90));
        if(mob.myworld.drawimage) {
//          g2d.drawImage(weapon.image, nx, ny, nw, nh, null);
          g2d.drawImage(mob.weapon.image, -nw/2, -nh/2, nw, nh, null);

        }
//        g.draw(new Rectangle(nx, ny, nw, nh));
        g.draw(new Rectangle(-nw/2, -nh/2, nw, nh));
        g.setColor(cur);
        g2d.rotate(Math.toRadians((4-mob.attackdirection)*90));
        g2d.translate(-nx, -ny);
      }
    } else {
      mob.attackdirection = 0;
    }
    if(!mob.dead) {
      g.setColor(Color.blue);
    } else {
      g.setColor(Color.black);
    }
    Color[] col = { g.getColor(), Color.red, new Color(0, 200, 0), Color.magenta, Color.cyan};
    if( mob.isStunned() ) {
      col[0] = Mob.BASH_COLOR;
    }
    if(mob.dead) {
      g.setColor(Color.black);
    }
    drawThing(g, mob, col);
    if(drawx>MINDRAWX && drawx<MAXDRAWX && drawy>MINDRAWY && drawy<MAXDRAWY && World.ZOOM == 1) {
      g.setColor(Color.white);
      int l = Integer.toString(mob.level).toCharArray().length;
      g.drawString(mob.level+"", drawx-l*5+2+distx, drawy+g.getFont().getSize()/2-4+disty);
      if( !mob.dead ) {
        g.setColor(new Color(200, 200, 200));
        double f = (double)mob.health/mob.getMaximumHealth();
        g.fillRect(drawx-w/2+distx, drawy-h/2-13+disty, mob.whiteline/10, 8);
        if(mob.whiteline/10>(f*w))
          mob.whiteline-=w/30;
        if(mob.whiteline/10<f*w)
          mob.whiteline = (int) (f*w*10);
        if(mob.isHostile()) {
          g.setColor(Color.red);
        } else {
          g.setColor(new Color( 0, 190, 20));
        }
        g.drawRect(drawx-w/2+distx, drawy-h/2-13+disty, w, 8);
        g.fillRect(drawx-w/2+distx, drawy-h/2-13+disty, (int) (f*w), 8);
      }
      
      if(mob instanceof Player) {
        g.setColor(new Color(150, 150, 0));
        g.drawRect(drawx-w/2, drawy-h/2-4, w, 4);
        g.setColor(Color.yellow);
        g.fillRect(drawx-w/2, drawy-h/2-4, (int) ((double)(mob.experience-mob.expatstartlvl)/(double)(mob.exptolvlup-mob.expatstartlvl)*w), 4);
      }
      else {
        int xPadding = 1;
        int itemWidth = 12/World.ZOOM;//(w - Mob.INV_SIZE*xPadding) / 5;
        for( int i = 0; i < mob.inv.size(); i++ ) {
          Item item = mob.inv.get(i);
          g.drawImage(item.image, drawx - w/2 + xPadding*(i+1) + itemWidth * i + distx, drawy + h/2 - itemWidth - xPadding + disty, itemWidth, itemWidth, null);
          
        }
      }
    }
	}
	public void drawPlayer( Graphics2D g, Player player ) {
	  int drawx = Panel.MIDX;
    int drawy = Panel.MIDY; 
    int w = player.w/World.ZOOM;
    int h = player.h/World.ZOOM;
    
    g.setColor(Color.green);
    if(player.attack!=null && player.adraw>=0) {
      int nx = player.attack.x-playerASDF.x+Panel.MIDX;
      int ny = player.attack.y-playerASDF.y+Panel.MIDY;
      int nw = player.attack.width;
      int nh = player.attack.height;
      if(player.attackdirection == 1 || player.attackdirection==3) {
        nw = player.attack.height;
        nh = player.attack.width;
      }
      Graphics2D g2d = (Graphics2D)g;
      g2d.translate(nx, ny);
      g2d.rotate(Math.toRadians(player.attackdirection*90));
      if(drawimage) {
        g2d.drawImage(player.weapon.image, -nw/2, -nh/2, nw, nh, null);
      }
      g.draw(new Rectangle(-nw/2, -nh/2, nw, nh));
      g2d.rotate(Math.toRadians((4-player.attackdirection)*90));
      g2d.translate(-nx, -ny);
    }
    if( player.isStunned() ) {
      g.setColor(Mob.BASH_COLOR);
    }
    else if(!player.dead) {
      g.setColor(Color.blue);
    }
    else {
      g.setColor(Color.black);
    }
    g.fill(new Rectangle(drawx-w/2, drawy-h/2, w, h));
    g.setColor(Color.white);
    int l = Integer.toString(player.level).toCharArray().length;
    g.drawString(player.level+"", drawx-l*5+2, drawy+6);
    
    g.setColor(new Color(200, 200, 200));
    double asd = (double)player.whiteline/10/player.getMaximumHealth();
    g.fillRect(drawx-w/2, drawy-h/2-13, (int) (asd*w), 8);
    if(player.whiteline/10>player.health)
      player.whiteline--;
    if(player.whiteline/10<player.health)
      player.whiteline = player.health*10;
    g.setColor(new Color( 0, 190, 20));
    g.drawRect(drawx-w/2, drawy-h/2-13, w, 8);
    double f = (double)player.health/player.getMaximumHealth();
    g.fillRect(drawx-w/2, drawy-h/2-13, (int) (f*w), 8);
    
    g.setColor(new Color(150, 150, 0));
    g.drawRect(drawx-w/2, drawy-h/2-4, w, 4);
    g.setColor(Color.yellow);
    g.fillRect(drawx-w/2, drawy-h/2-4, (int) ((double)(player.experience-player.expatstartlvl)/(double)(player.exptolvlup-player.expatstartlvl)*w), 4);
	}
	public void drawObstacle(Graphics2D g, Obstacle obst ) {

    if(obst.blockPlayer()) {
      Color[] colors = new Color[5];
      if( obst instanceof Sign ) {
        colors[0] = obst.color;
        for(int a=1; a<colors.length; a++) {
          colors[a] = World.darken(obst.color, ((a+1)/2)*50);
        }
      }
      else {
        colors[0] = obst.color;
        for(int a=1; a<colors.length && a<3; a++) {
          colors[a] = World.darken(obst.color, -90);
        }
        for(int a=3; a<colors.length; a++) {
          colors[a] = World.darken(obst.color, 90);
        }
      }
      drawThing(g, obst, colors);
    } else if ( DRAWPLAYEROBSTACLES ) {
      g.setColor(obst.color);
      int drawx = (obst.x-playerASDF.x)/World.ZOOM + Panel.MIDX;
      int drawy = (obst.y-playerASDF.y)/World.ZOOM + Panel.MIDY;
      g.drawRect(drawx-obst.w()/2/World.ZOOM, drawy-obst.h()/2/World.ZOOM, obst.w()/World.ZOOM, obst.h()/World.ZOOM);
    }
    if( obst instanceof Sign ) {
      Sign sign = (Sign) obst;
      int distx = 0;
      int disty = 0;
      int drawx = (sign.x-playerASDF.x)/World.ZOOM + Panel.MIDX;
      int drawy = (sign.y-playerASDF.y)/World.ZOOM + Panel.MIDY;
      int w = sign.w/World.ZOOM;
      int h = sign.h/World.ZOOM;
      if(drawx + w/2 >MINDRAWX && drawx - w/2 <MAXDRAWX && drawy + h/2 >MINDRAWY && drawy - h/2<MAXDRAWY) {
        Font cur = g.getFont();
        g.setFont(sign.font);
        g.setColor(Color.black);
        if(draw3d) {
          distx = (drawx-Panel.MIDX)/10;
          disty = (drawy-Panel.MIDY)/10;
        }
        g.drawImage(sign.skin, drawx-w/2+distx, drawy-h/2+disty, w, h, null);
        g.drawString(sign.message, drawx-w/2+1+sign.sx+distx, drawy+h/4+sign.sy+disty);
        g.setFont(cur);
      }
    }
  }
	public void drawShop( Graphics2D g, Shop shop ) {
	  int drawx = (shop.x-playerASDF.x)/World.ZOOM+Panel.MIDX;
    int drawy = (shop.y-playerASDF.y)/World.ZOOM+Panel.MIDY;
    int w = shop.w/World.ZOOM;
    int h = shop.h/World.ZOOM;
    if(drawx + w/2 >MINDRAWX && drawx - w/2 <MAXDRAWX && drawy + h/2 >MINDRAWY && drawy - h/2<MAXDRAWY) {
      Color cur = g.getColor();
      g.setColor(Color.yellow);
      g.fill(new Rectangle(drawx-w/2, drawy-h/2, w, h));
      g.setColor(Color.black);
      g.drawString(shop.getName(), drawx-w/2+10, drawy+g.getFont().getSize()/2);
      g.setColor(cur);
    }
	}
	public void drawThing(Graphics2D g, Thing thing, Color[] colors) {
	  drawThing(g, thing, colors, THREE_D_RATIO);
	}
  public void drawThing(Graphics2D g, Thing thing, Color[] colors, int threedRatio) {
    
    int drawx = (thing.x-playerASDF.x)/World.ZOOM+Panel.MIDX;
    int drawy = (thing.y-playerASDF.y)/World.ZOOM+Panel.MIDY;
    if(drawx + thing.w/2 >MINDRAWX && drawx - thing.w/2 <MAXDRAWX && drawy + thing.h/2 >MINDRAWY && drawy - thing.h/2<MAXDRAWY) {
      g.setColor(colors[0]);
      int w = thing.w/World.ZOOM;
      int h = thing.h/World.ZOOM;
      g.fillRect(drawx-w/2, drawy-h/2, w, h);
      int distx = 0;
      int disty = 0;
      if( draw3d && threedRatio > 0 ) {
        distx = (drawx-Panel.MIDX)/threedRatio;
        disty = (drawy-Panel.MIDY)/threedRatio;
        thing.poly.clear();

        MyPolygon bottom = new MyPolygon(colors[1]);
        bottom.addPoint(drawx-w/2+distx+w, drawy-h/2+disty+h);
        bottom.addPoint(drawx-w/2+w, drawy-h/2+h);
        bottom.addPoint(drawx-w/2, drawy-h/2+h);
        bottom.addPoint(drawx-w/2+distx, drawy-h/2+disty+h);

        MyPolygon right = new MyPolygon(colors[2]);
        right.addPoint(drawx-w/2+distx+w, drawy-h/2+disty+h);
        right.addPoint(drawx-w/2+w, drawy-h/2+h);
        right.addPoint(drawx-w/2+w, drawy-h/2);
        right.addPoint(drawx-w/2+distx+w, drawy-h/2+disty);

        MyPolygon left = new MyPolygon(colors[3]);
        left.addPoint(drawx-w/2+distx, drawy-h/2+disty);
        left.addPoint(drawx-w/2, drawy-h/2);
        left.addPoint(drawx-w/2, drawy-h/2+h);
        left.addPoint(drawx-w/2+distx, drawy-h/2+disty+h);
        
        MyPolygon top = new MyPolygon(colors[4]);
        top.addPoint(drawx-w/2+distx, drawy-h/2+disty);
        top.addPoint(drawx-w/2, drawy-h/2);
        top.addPoint(drawx-w/2+w, drawy-h/2);
        top.addPoint(drawx-w/2+distx+w, drawy-h/2+disty);
        
        if(distx>0) {
          if(disty>0) {
            thing.poly.add(right);
            thing.poly.add(bottom);
            thing.poly.add(top);
            thing.poly.add(left);
          } else {
            thing.poly.add(top);
            thing.poly.add(right);
            thing.poly.add(left);
            thing.poly.add(bottom);
          }
        } else {
          if(disty>0) {
            thing.poly.add(left);
            thing.poly.add(bottom);
            thing.poly.add(top);
            thing.poly.add(right);
          } else {
            thing.poly.add(top);
            thing.poly.add(left);
            thing.poly.add(right);
            thing.poly.add(bottom);
          }
        }
        
        for(int a=0; a<thing.poly.size(); a++) {
          MyPolygon po = thing.poly.get(a);
          g.setColor(po.color);
          g.fillPolygon(po);
        }
        g.setColor(colors[0]);
        g.fillRect(drawx-w/2+distx, drawy-h/2+disty, w, h);
      }
    }
  }
  
	public void updateMouseLocation(Point m) {
		mouse = m;
	}
	public void move() {
    Iterator<Mob> itmob = mobs.iterator();
    while( itmob.hasNext() ) {
      Mob mob = itmob.next();
			mob.move();
		}
		playerASDF.move();

    Iterator<Projectile> itproj = projectiles.iterator();
    while( itproj.hasNext() ) {
      Projectile p = itproj.next();
      p.move();
    }
    Iterator<ProjectileRegion> itprojregion = projectileRegions.iterator();
    while( itprojregion.hasNext() ) {
      ProjectileRegion region = itprojregion.next();
      region.move();
    }
    
		for(SoundArea s : sounds) {
			if(s.in(playerASDF.x(), playerASDF.y())) {
			  // only switch to new sound if music option set
			  if( playmusic ) {
			    changeSound(s.getSound());
			  }
			}
		}
	}
	/** 
	 * @return World.CANTMOVE or World.CANMOVE or a positive number which is the level of the dead collision.
	 */
	@SuppressWarnings("serial")
	public int collides(Mob what) {
	  if( NO_COLLISION ) {
	    return World.CANMOVE;
	  }
    Iterator<Mob> itmob = mobs.iterator();
    while( itmob.hasNext() ) {
      Mob temp = itmob.next();
			if(temp != what) {
				if(Math.abs(temp.x-what.x)<300 && Math.abs(temp.y-what.y)<300) {
					if(temp.dim().intersects(what.nextdim())) {
						if(temp.dead) {
							if(temp==snitch) {
								//TODO SNITCH
								int xp = temp.experience*4;
								mobs().remove(temp);
								int newx = (int)(Math.random()*1000);
								int newy = (int)(Math.random()*1700 -700);
								snitch = new Mob(newx, newy, "random hostile", this, getrace("Snitch")) {
									@Override
									public boolean damage(int d) {
										health-=d;
										dead = (health<=0);
										return dead;
									}
								};
								initializemob(snitch, getNextWeapon(temp.weapon.name));
								snitch.experience = xp;
								mobs.add(snitch);
							} else {
								mobs().remove(temp);
								for(Spawn s : spawns()) {
									if(s.isin(temp)) {
										mobs().add(s.remove(temp));
									}
								}
							}
							what.collectItems(temp);
							return temp.level;
						} else {
							return World.CANTMOVE;
						}
					}
				}
			}
		}
		Iterator<Obstacle> itwall = walls().iterator();
		while( itwall.hasNext() ) {
		  Obstacle wall = itwall.next();
		  if(wall.dim().intersects(what.nextdim())) {
        if(wall.blockPlayer()) {
          return World.CANTMOVE; 
        } else {
          if(what != playerASDF) {
            return World.CANTMOVE;
          }
        }
      }
		}
		if(what != playerASDF)
			if(playerASDF.dim().intersects(what.nextdim())) {
				return World.CANTMOVE; 
			}
		return World.CANMOVE;
	}
	
	
	public static String getNextWeapon(String weapon) {
		StringTokenizer st = new StringTokenizer(weapon);
		String type = st.nextToken();
		String weap = st.nextToken();
		if(type.equals("wooden")) { type = "iron"; 
		} else if(type.equals("iron")) { type = "steel";
		} else if(type.equals("steel")) { type = "mithril";
		} else if(type.equals("mithril")) { type = "adamant";
		} else if(type.equals("adamant")) { type = "rune";
		} else if(type.equals("rune")) { type = "dragon";
		} 
		return type + " " + weap;
	}
	public boolean collides(Mob what, Rectangle asdf) {
    Iterator<Mob> itmob = mobs.iterator();
    while( itmob.hasNext() ) {
      Mob temp = itmob.next();
			if(temp != what) {
				if(Math.abs(temp.x-asdf.x)<300 && Math.abs(temp.y-asdf.y)<300) {
					if(temp.dim().intersects(asdf)) {
						return true; 
					}
				}
			}
		}
		Iterator<Obstacle> itwall = walls().iterator();
    while( itwall.hasNext() ) {
      Obstacle wall = itwall.next();
//		for(int a=0; a<walls.size(); a++) {
//			Obstacle temp = walls.get(a);
			if(wall.dim().intersects(asdf)) {
				return true; 
			}
		}
		if(what != playerASDF)
			if(playerASDF.dim().intersects(asdf)) {
				return true; 
			}
		return false;
	}
	public boolean collides(Rectangle asdf) {
    Iterator<Mob> itmob = mobs.iterator();
    while( itmob.hasNext() ) {
      Mob temp = itmob.next();
			if(Math.abs(temp.x-asdf.x)<300 && Math.abs(temp.y-asdf.y)<300) {
				if(temp.dim().intersects(asdf)) {
					return true; 
				}
			}
		}
		Iterator<Obstacle> itwall = walls().iterator();
    while( itwall.hasNext() ) {
      Obstacle wall = itwall.next();
//		for(int a=0; a<walls.size(); a++) {
//			Obstacle temp = walls.get(a);
			if(wall.dim().intersects(asdf)) {
				return true; 
			}
		}
		if(playerASDF.dim().intersects(asdf)) {
			return true; 
		}
		return false;
	}
	public Queue<Mob> mobs() {
		return mobs;
	}
	public Queue<Obstacle> walls() {
		return walls;
	}
	public Queue<Shop> shops() {
		return shops;
	}
	public ArrayList<Spawn> spawns() {
		return spawns;
	}
	public void drawgui(Graphics2D g) {
		Color cur = g.getColor();
		g.setColor(new Color(210, 180, 180));
		g.fillRect(Panel.DIMX-Panel.GUIWIDTH, 0, Panel.GUIWIDTH, Panel.DIMY);
		Shop s = inshop();
		
		// draw health bar
    g.setColor(Color.red);
    g.fillRect(0, Panel.DIMY-Panel.GUIHEIGHT, Panel.DIMX, Panel.GUIHEIGHT);
    g.setColor(new Color( 0, 190, 20));
    g.fillRect(0, Panel.DIMY-Panel.GUIHEIGHT, (playerASDF.getCurrentHealth()*Panel.DIMX/playerASDF.getMaximumHealth()), Panel.GUIHEIGHT);
    g.setColor(Color.black);
    Font preFont = g.getFont();
    g.setFont(Constants.HEALTH_BAR_FONT);
    FontMetrics fm = g.getFontMetrics();
    String healthString = playerASDF.getCurrentHealth() + " / " + playerASDF.getMaximumHealth();
    g.drawString(healthString, (Panel.DIMX - fm.stringWidth(healthString))/2, Panel.DIMY - Panel.GUIHEIGHT + (g.getFont().getSize() + Panel.GUIHEIGHT - 6)/2 );
    g.setFont(preFont);
		if(s == null) {
			g.setColor(Color.black);
			playerASDF.inshop = false;
			selected = 0;
			g.setColor(Color.black);
			int yy = -5;
			int x1 = Panel.DIMX - Panel.GUIWIDTH + 30;
			int x2 = Panel.DIMX - Panel.GUIWIDTH + 130;
//			drawStat(g, x1, yy+=35, "Health", p.health);
//			drawStat(g, x2, yy, "TotalHealth", p.getMaximumHealth());
			drawStat(g, x1, yy+=35, "Money", playerASDF.money);
			drawStat(g, x2, yy, "Attack Delay", playerASDF.getAttackDelay());
			drawStat(g, x1, yy+=35, "Damage", playerASDF.getBaseDamage());
			drawStat(g, x2, yy, "Intelligence", playerASDF.getIntelligence());
			drawStat(g, x1, yy+=35, "Agility", playerASDF.getAgility());
			drawStat(g, x2, yy, "Strength", playerASDF.getStrength());
			drawStat(g, x1, yy+=35, "Speed", playerASDF.getAccel());
			drawStat(g, x2, yy, "Regen", playerASDF.getHealthRegen()*100);
			drawStat(g, x1, yy+=35, "X", playerASDF.x);
			drawStat(g, x2, yy, "Y", playerASDF.y);
			drawStat(g, x2, yy+=35, "Armor", 100-playerASDF.damagetaken);
			drawStat(g, x1, yy+=35, "Level", playerASDF.level);
			drawStat(g, x2, yy, "Experience", playerASDF.experience);
			//drawStat(g, x1, yy+=35, "Exptolvlup", p.exptolvlup);
			drawStat(g, x2, yy+=35, "Expleft", playerASDF.exptolvlup-playerASDF.experience);
			g.drawString("Race: "+playerASDF.race.name, x1, yy+=35);
		} else {
			playerASDF.inshop = true;
			drawshop(g, s);
		}
		playerASDF.drawinv(g, mouse);
		g.setColor(cur);
	}
	public Shop inshop() {
	  Iterator<Shop> itshop = shops.iterator();
	  while( itshop.hasNext() ) {
	    Shop s = itshop.next();
			if(s.dim().contains(playerASDF.dim())) {
				return s;
			}
		}
		return null;
	}
	public void drawshop(Graphics2D g, Shop s) {
    g.setColor(Color.blue);
    drawStat(g, Panel.DIMX-Panel.GUIWIDTH+20, 35, "Money", playerASDF.money);
    for(int a=0; a<s.onsale.size(); a++) {
      g.setColor(Color.blue);
      Item i = s.onsale.get(a);
      int xp = a%5;
      int yp = a/5;
      xp = Panel.DIMX-Panel.GUIWIDTH + 25 + xp*50;
      yp = 50+yp*50;
      i.draw(g, xp, yp, 40, 40, mouse, true);
      
      if( a == selected ) {
        g.setColor(Color.red);
        g.drawRect(xp-1, yp-1, 42, 42);
        g.drawRect(xp-2, yp-2, 44, 44);
      }
    }
	}
	public void drawStat(Graphics2D g, int x, int y, String name, int val) {
		if(val<=99999)
			g.drawString(name+": "+val, x, y);
		else
			g.drawString(name+": -----", x, y);
	}
	public void drawStat(Graphics2D g, int x, int y, String name, double val) {
		if(val<=99999)
			g.drawString(name+": "+(int)(val*100)/100.0, x, y);
		else
			g.drawString(name+": -----", x, y);
	}
	public String encrypt(String s) {
		char[] one = s.toCharArray();
		String to = "";
		int delta = 1;
		for(int a=0; a<one.length; a++) {
			int num = (int)(one[a])+delta;
			delta = delta*-1;
			one[a] = (char)(num);
			to += one[a]+"";
		}
		return to;
	}
	public String decrypt(String s) {
		char[] one = s.toCharArray();
		String to = "";
		int delta = 1;
		for(int a=0; a<one.length; a++) {
			int num = (int)(one[a])-delta;
			delta = delta*-1;
			one[a] = (char)(num);
			to += one[a]+"";
		}
		return to;
	}
	public void save(String slot) {
		
		messages.add(new Message("saving", 50));
		try {
			// Create file
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("saves\\"+slot+".sav")));
			ArrayList<String> towrite = new ArrayList<String>();
			towrite.add(playerASDF.tosave());
			ArrayList<Mob> inaspawn = new ArrayList<Mob>();
			for(Spawn s : spawns) {
				for(Mob m : s.mobs) {
					inaspawn.add(m);
				}
				towrite.add(s.tosave());
			}
			for(Mob m : mobs) {
				if(!inaspawn.contains(m)) {
					towrite.add(m.tosave());
				}
			}
			for(Obstacle w : walls) {
				towrite.add(w.tosave());
			}
			for(Sign s : signs) {
				towrite.add(s.tosave());
			}
			out.println(towrite.size());
			for(String s : towrite) {
				out.println(s);//encrypt(s));
			}
			// Close the output stream
			out.close();
			
			messages.add(new Message("save complete", 100));
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
			messages.add(new Message("error saving", 100));
		}
	}

	public void load(String slot) {
		System.out.println("Loading stats: ");
		messages.add(new Message("loading "+slot, 50));
		boolean hasclearedmobs = false;
		boolean hasclearedwalls = false;
		boolean hasclearedsigns = false;
		boolean hasclearedspawns = false;
		
//		String a = "";
//		int xx = 0, yy = 0, agi = 0, str = 0, intel = 0, money = 0, accel = 0, health = 0;
//		int lvl = 0, exp = 0;
//		String race = "";
//		String weap = "";
//		ArrayList<Item> its = new ArrayList<Item>();

		try {
			BufferedReader buff = new BufferedReader(new FileReader("saves\\" + slot + ".sav"));
			int numofthings = Integer.parseInt(buff.readLine());
			if(!hasclearedmobs) {
				mobs = new ConcurrentLinkedQueue<Mob>();
				hasclearedmobs = true;
			}
			for(int a=0; a<numofthings; a++) {
				String line = /*decrypt(*/buff.readLine();//);
				StringTokenizer st = new StringTokenizer(line);
				String name = st.nextToken();
				if(name.equals("Player")) {
					int xx = 0, yy = 0, money = 0, /*agi = 0, str = 0, intel = 0, , accel = 0, */health = 0;
					int exp = 0;
					String race = "", weap = "";
					ArrayList<Item> its = new ArrayList<Item>();
					String[] temp = line.split("[,]");
					String stats = temp[0];
					String inv = temp[1];
					st = new StringTokenizer(stats);
					st.nextToken();
					race = st.nextToken();
					exp = Integer.parseInt(st.nextToken());
					money = Integer.parseInt(st.nextToken());
					xx = Integer.parseInt(st.nextToken());
					yy = Integer.parseInt(st.nextToken());
					health = Integer.parseInt(st.nextToken());
					weap = st.nextToken();
					while(st.hasMoreTokens()) {
						weap += ' '+st.nextToken();
					}
					System.out.println(inv);
					st = new StringTokenizer(inv);
					int numofitems = Integer.parseInt(st.nextToken());
					for(int b = 0; b<numofitems; b++) {
						String nameofitem = st.nextToken();
						nameofitem = nameofitem.replace('_', ' ');
						int numoftheitem = Integer.parseInt(st.nextToken());
						its.add(new Item(nameofitem, numoftheitem, this));
					}
					initPlayer(xx, yy, weap, money, its, race, exp, health);
				}
				if(name.equals("Mob")) {
					String stats = line;
					st = new StringTokenizer(stats);
					loadMob(st);
				}
				String skinloc = "";
				String message = "";
				boolean issign = false;
				if(name.equals("Sign")) {
					if(!hasclearedsigns) {
						signs = new ConcurrentLinkedQueue<Sign>();
						hasclearedsigns = true;
					}
					skinloc = st.nextToken();
					message = st.nextToken();
					// need to skip a token for some reason
					st.nextToken();
					issign = true;
				}
				if(name.equals("Wall") || issign) {
					if(!issign) {
						if(!hasclearedwalls) {
							walls = new ConcurrentLinkedQueue<Obstacle>();
							hasclearedwalls = true;
						}
					}
					int xx = 0, yy = 0, ww = 0, hh = 0, re = 0, gr = 0, bl = 0, blocky = 0;
					xx = Integer.parseInt(st.nextToken());
					yy = Integer.parseInt(st.nextToken());
					ww = Integer.parseInt(st.nextToken());
					hh = Integer.parseInt(st.nextToken());
					re = Integer.parseInt(st.nextToken());
					gr = Integer.parseInt(st.nextToken());
					bl = Integer.parseInt(st.nextToken());
					blocky = Integer.parseInt(st.nextToken());
					boolean block = false;
					if(blocky==1)
						block = true;
					if(issign) {
						initSign(xx, yy, ww, hh, skinloc, message);
					} else {
						initWall(xx, yy, ww, hh, re, gr, bl, block);
					}
				}
				if(name.equals("Spawn")) {
					System.out.println("Loadingspawn");
					if(!hasclearedspawns) {
						spawns = new ArrayList<Spawn>();
						hasclearedspawns = true;
					}
					int xx = Integer.parseInt(st.nextToken());
					int yy = Integer.parseInt(st.nextToken());
					int ww = Integer.parseInt(st.nextToken());
					int hh = Integer.parseInt(st.nextToken());
					int experience = Integer.parseInt(st.nextToken());
					int numofmobs = Integer.parseInt(st.nextToken());
					Spawn spa = new Spawn(xx, yy, ww, hh, this, experience);
					for(int b=0; b<numofmobs; b++) {
//						st.nextToken();// to go over "Mob"
						Mob m = loadMob(st);
						spa.addmob(m);
					}
					spawns.add(spa);
				}
			}
      buff.close();
		} catch (Exception e) {// Catch exception if any
			//System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			messages.add(new Message("Error loading "+slot+"!", 100));
			return;
		}
		System.out.println("Done loading");
	}
	public Mob loadMob(StringTokenizer st) {
		int xx = 0, yy = 0, /*agi = 0, str = 0, intel = 0, accel = 0, */money = 0, health = 0;
		int exp = 0;
		String race = "", weap = "", ai = "";
//  Need to skip a token for some reason
		st.nextToken();
		race = st.nextToken();
		exp = Integer.parseInt(st.nextToken());
		money = Integer.parseInt(st.nextToken());
		xx = Integer.parseInt(st.nextToken());
		yy = Integer.parseInt(st.nextToken());
		health = Integer.parseInt(st.nextToken());
		weap = st.nextToken().replace('_', ' ');
		ai = st.nextToken().replace('_', ' ');
		return initMob(race, exp, money, xx, yy, health, weap, ai);
	}
	public void initializeworld() {
		loadraces(); // TODO continue debug output from here 

		//THIS IS ONLY HERE TO PREVENT NULL POINTER EXCEPTIONS
		initPlayer(-1150, -950, "dagger", 0, new ArrayList<Item>(), "Human", 0);
//    initPlayer(0, 0, "dagger", 0, new ArrayList<Item>(), "Human", 0);
		
		Mob newmob = new Mob(300, 300, "bettermovetowardsyou hostile", this, getrace("Dwarf"));
		mobs.add(newmob);
		initializemob(newmob, "wooden spear");
		
		newmob = new Mob(400, 300, "sway hostile", this, getrace("Elf"));
		mobs.add(newmob);
		initializemob(newmob, "wooden dagger");
		newmob = new Mob(300, 400, "zigzag hostile", this, getrace("Elf"));
		mobs.add(newmob);
		initializemob(newmob, "wooden sword");
		
		
		//TODO GHOST AREA
		// border
		walls.add(new Obstacle(-550, -2850, 900, 300, this, Color.darkGray, true));
		walls.add(new Obstacle(700, -3750, 400, 700, this, Color.darkGray, true));
		walls.add(new Obstacle(-250, -4250, 2300, 300, this, Color.darkGray, true));
		walls.add(new Obstacle(-1600, -3450, 400, 1900, this, Color.darkGray, true));
		// right side left side lich walls
    walls.add(new Obstacle(150, -3650, 700, 100, this, Color.darkGray, true));
    walls.add(new Obstacle(150, -3850, 700, 100, this, Color.darkGray, true));
    walls.add(new Obstacle(-1050, -3450, 700, 100, this, Color.darkGray, true));
    walls.add(new Obstacle(-1050, -3650, 700, 100, this, Color.darkGray, true));
    walls.add(new Obstacle(-1050, -3850, 700, 100, this, Color.darkGray, true));
    walls.add(new Obstacle(-1050, -4050, 700, 100, this, Color.darkGray, true));
    // middle divider
    walls.add(new Obstacle(-450, -3500, 200, 1000, this, Color.darkGray, true));
    // boss obstruction
    walls.add(new Obstacle(-825, -3200, 200, 200, this, Color.darkGray, true));

    newmob = new Mob(440, -3750, "leftattack", this, getrace("Lich"));
    newmob.lvlupto(30);
    mobs.add(newmob);
    initializemob(newmob, "ghost beam");
    newmob = new Mob(-1345, -3550, "rightattack", this, getrace("Lich"));
    newmob.lvlupto(30);
    mobs.add(newmob);
    initializemob(newmob, "ghost beam");
    newmob = new Mob(-1345, -3750, "rightattack", this, getrace("Lich"));
    newmob.lvlupto(30);
    mobs.add(newmob);
    initializemob(newmob, "ghost beam");
    newmob = new Mob(-1345, -3950, "rightattack", this, getrace("Lich"));
    newmob.lvlupto(30);
    mobs.add(newmob);
    initializemob(newmob, "ghost beam");
    newmob = new Mob(-275, -4050, "downattack", this, getrace("Lich"));
    newmob.lvlupto(30);
    mobs.add(newmob);
    initializemob(newmob, "ghost beam");
    
    Shop shoptoadd = new Shop(240, -4000, 150, 150, this, "Greater Rings");
    shops.add(shoptoadd);
    shoptoadd.onsale.add(new Item("greaterregenring", 99, this));
    shoptoadd.onsale.add(new Item("greaterhealthring", 99, this));
    shoptoadd.onsale.add(new Item("greaterstrengthring", 99, this));
    shoptoadd.onsale.add(new Item("greaterintelligencering", 99, this));
    shoptoadd.onsale.add(new Item("greaterstatring", 99, this));
    shoptoadd.onsale.add(new Item("greatercritring", 99, this));
    
		newmob = new Mob(-1200, -2850, "random hostile nomiss", this, getrace("bigboss"));
		mobs.add(newmob);
		initializemob(newmob, "ghost bomb");
		newmob.lvlupto(60);
		shoptoadd = new Shop(-1200, -2850, 120, 120, this, "Dragon");
		shops.add(shoptoadd);
		for(int b=0; b<=5; b++) {
			String u = "";
			if(b==0)
				u = "sword";
			if(b == 1)
				u = "dagger";
			if(b==2)
				u = "spear";
			if(b==3)
				u = "mace";
			if(b == 4)
				u = "battleaxe";
			if(b == 5)
				u = "squareshield";
			if(b == 5) {
			  shoptoadd.onsale.add(new Item("dragon "+u, 9, this));
			} else {
			  shoptoadd.onsale.add(new Weapon("dragon "+u, 9, this));
			}
		}
		walls.add(new Obstacle(-700, -2520, 1000, 100, this, Color.darkGray, true));
		
		//TODO LAVA
		walls.add(new Obstacle(2170, -1600, 300, 850, this, Color.red, true));
		walls.add(new Obstacle(2500, -2000, 400, 100, this, Color.red, true));
		shoptoadd = new Shop(3000, -3100, 100, 100, this, "Rune");
		shops.add(shoptoadd);
		for(int b=0; b<=5; b++) {
			String u = "";
			if(b==0)
				u = "sword";
			if(b == 1)
				u = "dagger";
			if(b==2)
				u = "spear";
			if(b==3)
				u = "mace";
			if(b == 4)
				u = "battleaxe";
			if(b == 5)
				u = "squareshield";
			if(b == 5) {
			  shoptoadd.onsale.add(new Item("rune "+u, 9, this));
			} else {
			  shoptoadd.onsale.add(new Weapon("rune "+u, 9, this));
			}
		}
		Mob toadd;
		for(int y = -1900; y <= -1300; y += 100) {
			toadd = new Mob(2600, y, "horizontalpatrol hostile nomiss", this, getrace("Patrol"));
			initializemob(toadd, "rune deathaura");
			toadd.lvlupto(20);
			mobs.add(toadd);
		}
		
		spawns.add(new Spawn(2750, -2800, 500, 700, this, 50) {
			private static final long serialVersionUID = 1L;
			@Override
			public Mob remove(Mob m) {
				this.experience+=10;
				return super.remove(m);
			}
		});
		toadd = new Mob(2800, -3000, "random nomiss hostile", this, getrace("Super_Ninja"));
		initializemob(toadd, "rune sword");
		spawns.get(spawns.size()-1).addmob(toadd);
		mobs.add(toadd);
		
		walls.add(new Obstacle(1150, -2000, 1650, 100, this, Color.red, true));
		walls.add(new Obstacle(1380, -1670, 30, 550, this, Color.red, false));
		walls.add(new Obstacle(840, -1420, 1050, 40, this, Color.red, true));
		
		for(int xp=330; xp<=1300; xp+=125) {
			for(int yp=-1725; yp<-1440; yp+=125) {
				String w = "rune ";
				int bl = rand.nextInt(4);
				if(bl == 0) {
					w+="sword";
				} else if( bl==1) {
					w+="spear";
				} else if( bl==2) {
					w+="mace";
				} else if( bl==3) {
					w+="dagger";
				}
				w = "rune sword";
				toadd = new Mob(xp, yp, "zigzag hostile", this, getrace("Warrior"));
				mobs.add(toadd);
				initializemob(toadd, w);
				toadd.lvlupto(15+rand.nextInt(10));
			}
		}
			
		
		
		walls.add(new Obstacle(100, -2000, 400, 2000, this, Color.red, true));
		walls.add(new Obstacle(2300, -700, 2000, 200, this, Color.red, true));
		walls.add(new Obstacle(3300, -2000, 400, 3000, this, Color.red, true));
		walls.add(new Obstacle(2300, -3300, 3600, 200, this, Color.red, true));
		newmob = new Mob(400, -3100, "random hostile", this, getrace("bigboss"));
		mobs.add(newmob);
		
		initializemob(newmob, "dragon bomb");
		newmob.lvlupto(40);
		
//		walls.add(new Obstacle(850, -950, 110, 110, this, Color.orange, false));
//		walls.add(new Obstacle(950, -850, 110, 110, this, Color.orange, false));
		walls.add(new Obstacle(900, -900, 400, 400, this, Color.orange, false));

		walls.add(new Obstacle(2400, -2800, 100, 900, this, Color.red, true));
		walls.add(new Obstacle(2650, -2300, 650, 100, this, Color.red, true));
		walls.add(new Obstacle(3050, -2300, 150, 150, this, Color.red, false));
		
		
		//end lava area
		
		//blue border
		walls.add(new Obstacle(-300, -1200, 2200, 400, this, Color.blue, true));
		// TODO left wall
		walls.add(new Obstacle(-1200, 300, 400, 2200, this, Color.black, true));
		walls.add(new Obstacle(-1300, -900, 200, 200, this, Color.black, true));
		walls.add(new Obstacle(200, 1200, 3200, 400, this, Color.blue, true));

		// south east of arena
		shoptoadd = new Shop(-950, 950, 100, 100, this, "Steel");
		shops.add(shoptoadd);
		for(int b=0; b<=5; b++) {
			String u = "";
			if(b==0)
				u = "sword";
			if(b == 1)
				u = "dagger";
			if(b==2)
				u = "spear";
			if(b==3)
				u = "mace";
			if(b == 4)
				u = "battleaxe";
			if(b == 5)
				u = "squareshield";
			if(b == 5) {
			  shoptoadd.onsale.add(new Item("steel "+u, 9, this));
			} else {
			  shoptoadd.onsale.add(new Weapon("steel "+u, 9, this));
			}
		}
		//
		
		//north east of mountains shop
		shoptoadd = new Shop(-1100, -900, 100, 100, this, "Mithril");
		shops.add(shoptoadd);
		for(int b=0; b<=5; b++) {
			String u = "";
			if(b==0)
				u = "sword";
			if(b == 1)
				u = "dagger";
			if(b==2)
				u = "spear";
			if(b==3)
				u = "mace";
			if(b == 4)
				u = "battleaxe";
			if(b == 5)
				u = "squareshield";
			if(b == 5) {
			  shoptoadd.onsale.add(new Item("mithril "+u, 9, this));
			} else {
			  shoptoadd.onsale.add(new Weapon("mithril "+u, 9, this));
			}
		}
		//
		walls.add(new Sign(150, 0, 150, 30, this, "baseleft", "Mountains"));
		
		// arena area
		walls.add(new Sign(50, 500, 95, 30, this, "baseleft", "Arena"));
		
		walls.add(new Obstacle(1200, 530, 400, 660, this,Color.blue, true));
		walls.add(new Obstacle(1800, 500, 100, 1000, this, Color.blue, true));
		walls.add(new Obstacle(1550, 20, 400, 100, this, Color.blue, true));
		walls.add(new Obstacle(1200, -350, 400, 900, this, Color.blue, true));
		//end arena area
		
		walls.add(new Sign(1200, 800, 145, 40, this, "baseright", "Training"));
		
		// TODO separate wood and iron shop
		shoptoadd = new Shop(1200, 150, 400, 100, this, "Wooden / Iron");
		shops.add(shoptoadd);
		for(int a=1; a<=2; a++) {
			String t = "";
			if(a==1) 
				t = "wooden ";
			if(a==2)
				t = "iron ";
			for(int b=0; b<=4; b++) {
				String u = "";
				if(b==0)
					u = "sword";
				if(b == 1)
					u = "dagger";
				if(b==2)
					u = "spear";
				if(b==3)
					u = "mace";
				if(b == 4)
					u = "battleaxe";
			  shoptoadd.onsale.add(new Weapon(t+u, 9, this));
			}
		}
		for(int a=1; a<=2; a++) {
      String t = "";
      if(a==1) 
        t = "wooden ";
      if(a==2)
        t = "iron ";
      String u = "squareshield";
      shoptoadd.onsale.add(new Item(t+u, 9, this));
    }
		
		shoptoadd = new Shop(1000, 0, 400, 100, this, "Trader");
		shops.add(shoptoadd);
//		for(int b=5; b<6; b++) {
//		String u = "";
//		if(b == 5)
//			u = "squareshield";
//		for(int a=1; a<=7; a++) {
//			String t = "";
//			if(a==1) 
//				t = "wooden ";
//			if(a==2)
//				t = "iron ";
//			if(a==3) 
//				t = "steel ";
//			if(a==4) 
//				t = "mithril ";
//			if(a==5) 
//				t = "adamant ";
//			if(a==6)
//				t = "rune ";
//			if(a==7)
//				t = "dragon ";
//			shops.get(shops.size()-1).onsale.add(new Item(t+u, 9, this));
//		}
//	}
	
	
		shoptoadd.onsale.add(new Item("elvishboots", 99, this));
		shoptoadd.onsale.add(new Weapon("laser", 9, this));
		shoptoadd.onsale.add(new Weapon("spartan laser", 9, this));
		shoptoadd.onsale.add(new Weapon("pistol", 9, this));
		shoptoadd.onsale.add(new Weapon("sniper rifle", 9, this));
		//shops.get(0).buy(5);
		walls.add(new Obstacle(1420, 150, 40, 400, this, Color.blue, true));
		
		shoptoadd = new Shop(100, -800, 100, 100, this, "Rings");
		shops.add(shoptoadd);
		shoptoadd.onsale.add(new Item("regenring", 99, this));
		shoptoadd.onsale.add(new Item("healthring", 99, this));
		shoptoadd.onsale.add(new Item("agilityring", 99, this));
		shoptoadd.onsale.add(new Item("strengthring", 99, this));
		shoptoadd.onsale.add(new Item("intelligencering", 99, this));
		shoptoadd.onsale.add(new Item("statring", 99, this));
		shoptoadd.onsale.add(new Item("critring", 99, this));
		
		
		walls.add(new Obstacle(500, 780, 600, 40, this, Color.blue, true));
		walls.add(new Obstacle(780, -100, 40, 1400, this, Color.blue, true));
		walls.add(new Obstacle(680, -100, 50, 50, this, Color.blue, true));
		walls.add(new Sign(1050, 35, 90, 60, this, "baseup", "Boss"));
		
		walls.add(new Obstacle(400, 610, 150, 40, this, Color.blue, true));
		
		shoptoadd = new Shop(400, 675, 100, 100, this, "Shop");
		shops.add(shoptoadd);
		shoptoadd.onsale.add(new Item("boots", 9, this));
		shoptoadd.onsale.add(new Weapon("wooden sword", 9, this));
		shoptoadd.onsale.add(new Weapon("wooden dagger", 9, this));
		shoptoadd.onsale.add(new Weapon("shortbow", 9, this));
		shoptoadd.onsale.add(new Weapon("longbow", 9, this));
		shoptoadd.onsale.add(new Item("statring", 9, this));
		shoptoadd.onsale.add(new Item("wooden squareshield", 9, this));
		
		newmob = new Mob(900, -900, "random hostile", this, getrace("bigboss"));
		mobs.add(newmob);
		initializemob(newmob, "rune bomb");
		newmob.lvlupto(15);
		
		
		//first boss shop
		shoptoadd = new Shop(1050, -1050, 100, 100, this, "Adamant");
		shops.add(shoptoadd);
		for(int b=0; b<=4; b++) {
			String u = "";
			if(b==0)
				u = "sword";
			if(b == 1)
				u = "dagger";
			if(b==2)
				u = "spear";
			if(b==3)
				u = "mace";
			if(b == 4)
				u = "battleaxe";
			if(b == 5)
				u = "squareshield";
			if(b == 5) {
			  shoptoadd.onsale.add(new Item("adamant "+u, 9, this));
			} else {
			  shoptoadd.onsale.add(new Weapon("adamant "+u, 9, this));
			}
		}
//		shoptoadd.onsale.add(new Weapon("rune bomb", 99, this));
//		shoptoadd.onsale.add(new Weapon("diamond laser", 99, this));
		shoptoadd.onsale.add(new Weapon("diamond dagger", 99, this));
		shoptoadd.onsale.add(new Weapon("test", 99, this));
		
		
		walls.add(new Obstacle(-550, 100, 940, 40, this, Color.blue, true));
		walls.add(new Obstacle(-100, 500, 40, 800, this, Color.blue, true));
		walls.add(new Obstacle(0, 920, 400, 20, this, Color.blue, true));
		

		walls.add(new Obstacle(1200, 940, 400, 150, this, Color.black, false));
		spawns.add(new Spawn(1550, 500, 300, 800, this, 10));
		toadd = new Mob(1500, 400, "random hostile", this, getrace("fatdummy"));
		initializemob(toadd, "iron deathaura");
		spawns.get(spawns.size()-1).addmob(toadd);
		mobs.add(toadd);
		
		toadd = new Mob(1600, 400, "random hostile", this, getrace("smalldummy"));
		initializemob(toadd, "iron dagger");
		spawns.get(spawns.size()-1).addmob(toadd);
		mobs.add(toadd);

		//TODO SNITCH
		snitch = new Mob(-1100, -900, "random hostile", this, getrace("Snitch"));
		initializemob(snitch, "wooden mace");
		snitch.experience = 10000;
		mobs.add(snitch);
		
		//type is a very low chance of weird arena setups
		double type1 = Math.random();
		double type2 = Math.random();
		double weaponchance = .03;
		double typechance = .03;
		for(int a=0; a<9; a++) {
			for(int b=2; b<=9; b++) {
				int yp = a*85+200;
				int xp= -b*100+rand.nextInt(200)-100;
				Race r = races.get(rand.nextInt(2));
				Rectangle  di = new Rectangle(xp-r.startwidth/2, yp-r.startheight/2, r.startwidth, r.startheight);
				if(!collides(di)) {
					String w = "";
					int bl = rand.nextInt(4);
					int al = rand.nextInt(7);
					if(type1<weaponchance) {
						bl = 0;
					} else if(type1<weaponchance*2) {
						bl = 1;
					} else if(type1<weaponchance*3) {
						bl = 2;
					} else if(type1<weaponchance*4) {
						bl = 3;
					}
					if(type2<typechance) {
						al = 0;
					} else if(type2<typechance*2) {
						al = 1;
					} else if(type2<typechance*3) {
						al = 2;
					} else if(type2<typechance*4) {
						al = 3;
					} else if(type2<typechance*5) {
						al = 4;
					} else if(type2<typechance*6) {
						al = 5;
					} else if(type2<typechance*7) {
						al = 6;
					}
					if(al == 0) {
						w += "wooden ";
					} else if(al == 1) {
						w += "iron ";
					} else if(al == 2) {
						w += "steel ";
					} else if(al == 3) {
						w += "mithril ";
					} else if(al == 4) {
						w += "adamant ";
					} else if(al == 5) {
						w += "rune ";
					} else if(al == 6) {
						w += "dragon ";
					}
					if(bl == 0) {
						w+="dagger";
					} else if( bl==1) {
						w+="spear";
					} else if( bl==2) {
						w+="mace";
					} else if( bl==3) {
						w+="sword";
					}
					newmob = new Mob(xp, yp, "random hostile", this, r);
					double rand = Math.random();
					double chance = 0;
					double ringChance = 0.1;
          if (rand < (chance+=ringChance)) {
            String ring = Item.getRandomRing();
            if( Math.random() < 0.01 ) {
              ring = "greater" + ring;
            }
            newmob.addItem(new Item(ring, 1, this));
          } else if (rand < (chance+=ringChance)) {
            newmob.addItem(new Item(Item.getRandomRankRoulet() + " squareshield", 1, this));
          } else if (rand < (chance+=ringChance)) {
            if( Math.random() < 0.75 ) {
              newmob.addItem(new Item("boots", 1, this));
            }
            else {
              newmob.addItem(new Item("elvishboots", 1, this));
            }
          }
          mobs.add(newmob);
          initializemob(newmob, w);
				}
			}
		}
		
		// mountains
		walls.add(new Obstacle(-60, -450, 50, 1100, this, Color.black, false));
		for(int b=1; b<=82; b++) {
			int yp = -rand.nextInt(1000)+40;
			int xp= -rand.nextInt(1000)+40;
			int xsize = rand.nextInt(60)+10;
			int ysize = rand.nextInt(60)+10;
			
			//Rectangle di = new Rectangle(xp-xsize/2, yp-ysize/2, xsize, ysize);
			int c = rand.nextInt(4);
			Color co ;
			if(c == 0) 
				co = new Color(139, 69, 19);
			else if(c==1)
				co = new Color(160,82,45);
			else if(c == 2)
				co = new Color(128,128,0);
			else
				co= new Color(85,107,47);
			walls.add(new Obstacle(xp, yp, xsize, ysize, this, co, true));
		}
		// TODO Icy mountains
//		walls.add(new Obstacle(-2000, -2000, 3800, 1200, this, Color.black, true));
//    walls.add(new Obstacle(-2050, -500, 900, 4000, this, Color.black, true));
//    walls.add(new Obstacle(-2050, 1600, 900, 200, this, Color.lightGray, true));
		walls.add(new Obstacle(-2050, 700, 900, 100, this, Color.yellow, false));
    walls.add(new Obstacle(-2350, 1500, 300, 400, this, Color.lightGray, true));
    walls.add(new Obstacle(-1750, 1500, 300, 400, this, Color.lightGray, true));
    walls.add(new Obstacle(-2250, -2600, 900, 200, this, Color.lightGray, true));
    walls.add(new Obstacle(-1500, 0, 200, 3400, this, Color.lightGray, true));
    walls.add(new Obstacle(-1500, -2100, 200, 800, this, Color.yellow, false));
    walls.add(new Obstacle(-2600, -400, 200, 4200, this, Color.lightGray, true));

    newmob = new Mob(-2050, 1500, "random hostile nomiss", this, getrace("bigboss"));
    mobs.add(newmob);
    initializemob(newmob, "frost bomb");
    newmob.lvlupto(80);
    
    ProjectileRegion region = new ProjectileRegion(-2500, -2550, 900, 3850, this, 200) {
      @Override
      public Point getNewSpawnLocation() {
        double tx = (int)(w*Math.random()*2/3);
        if( tx > w/3 ) {
          tx += w/3;
        }
        return new Point((int) (x + tx), this.y + this.h);
      }
      @Override
      public Point getNewTargetLocation() {
        return new Point((int) (x + w*Math.random()), this.y);
      }
      @Override
      public float getSpeed() {
        return (float) (29F + Math.random());
      }
    };
    projectileRegions.add(region);
    for(int b=0; b<200; b++) {
      int xsize = rand.nextInt(70)+20;
      int ysize = rand.nextInt(70)+20;
      int xp = rand.nextInt( 900) - 2500;
      int yp = 0;
      if( b < 50 ) {
        yp = rand.nextInt(950) - 2500;
      } else if( b < 100 ) {
        yp = rand.nextInt(950) - 2500 + 950;
      } else if( b < 150 ) {
        yp = rand.nextInt(950) - 2500 + 950 + 950;
      } else if( b < 200 ) {
        yp = rand.nextInt(950) - 2500 + 950 + 950 + 950;
      }
      if( xp > -1600 - xsize/2 ) {
        xp = -1600 - xsize/2;
      }
      if( yp > 1300 - ysize/2 ) {
        yp = 1300 - ysize/2;
      }
      if( xp < -2500 + xsize/2 ) {
        xp = xsize/2 - 2500;
      }
      if( yp < -2500 + ysize/2 ) {
        yp = ysize/2 - 2500;
      }
      int c = rand.nextInt(4);
      Color co ;
      if(c == 0) 
        co = new Color(175 + rand.nextInt(20), 175 + rand.nextInt(20), 200 + rand.nextInt(20));
      else if(c==1)
        co = new Color(175 + rand.nextInt(20), 170 + rand.nextInt(20), 190 + rand.nextInt(20));
      else if(c == 2)
        co = new Color(175 + rand.nextInt(20), 185 + rand.nextInt(20), 200 + rand.nextInt(20));
      else
        co= new Color(185 + rand.nextInt(20), 185 + rand.nextInt(20), 210 + rand.nextInt(20));
      walls.add(new Obstacle(xp, yp, xsize, ysize, this, co, true));
    }
		
		spawns.add(new Spawn(-525, -525, 750, 750, this, 10));
		for(int a=0; a<5; a++) {
			int yp = -rand.nextInt(750)-150;
			int xp = -rand.nextInt(750)-150;
			Race r = getrace("Goat");
			Rectangle di = new Rectangle(xp-r.startwidth, yp-r.startheight, r.startwidth, r.startheight);
			while(collides(di)) {
				yp = -rand.nextInt(750)-150;
				xp = -rand.nextInt(750)-150;
				di = new Rectangle(xp-r.startwidth, yp-r.startheight, r.startwidth, r.startheight);
			}
			toadd = new Mob(xp, yp, "random hostile", this, r);
			initializemob(toadd, "steel sword");
			spawns.get(spawns.size()-1).addmob(toadd);
			mobs.add(toadd);
		}
	}
	public void loadraces() {
		Frame.print("Loading Races: ");
		races = new ArrayList<Race>();
		// name, agility increase, strength increase, damage increase
		// agility, strength, intelligence, damage
		// health, speed, regen
		// width, height, armor
		races.add(new Race("Elf", 4, 7, .1, 
				25, 30, 12, 6, 
				20, 8, .01, 
				36, 36, 3));
		
		races.add(new Race("Human", 3, 9, .2, 
				20, 40, 11, 7, 
				25, 6, .05, 
				38, 38, 4));
		
		races.add(new Race("Dwarf", 2, 11, .3, 
				15, 50, 10, 9, 
				30, 5, .1, 
				40, 40, 5));
		
		races.add(new Race("Warrior", 3, 9, .5, 
				25, 50, 9, 10, 
				30, 6, .09, 
				39, 39, 10));
		
		races.add(new Race("Patrol", 3, 9, .5, 
				25, 50, 9, 10, 
				30, 15, .09, 
				39, 39, 10));
		
		races.add(new Race("Scholar", 3, 8, .1,
				18, 38, 30, 6, 
				24, 6, .04, 
				37, 37, 5));
		
		races.add(new Race("Assassin", 1, 5, 1.5, 
				30, 30, 10, 10, 
				20, 8, .09, 
				35, 35, 2));
			
		races.add(new Race("Super_Ninja", 12, 18, 3,
				10, 30, 25, 5,
				20, 10, .05,
				38, 38, 4));
		
		races.add(new Race("Goat", 2, 3, 1, 
		    100, 10, 1, 15, 
		    10, 10, 1, 
		    25, 25, 3));
		races.add(new Race("Snitch", 1, 1, 1, 
		    1, 1, 1, 10, 
		    1, 25, 0, 
		    1, 10, 3));
    races.add(new Race("Lich", 10, 10, 10, 
        50, 50, 10, 10, 
        100, 0, 0.3, 
        60, 60, 40));
		races.add(new Race("bigboss", 5, 13, 2, 
		    50, 100, 16, 10, 
		    100, 0, .5, 
		    180, 180, 25));
		races.add(new Race("fatdummy", 0, 11, 0, 
		    1, 150, 1, 1, 
		    150, 4, 1, 
		    60, 60, 10));
		races.add(new Race("smalldummy", 0, 21, 0, 
		    100, 15, 1, 1, 
		    1, 8, 0, 
		    40, 40, 1));
		races.add(new Race("Baal", 0, 0, 0, 
		    999999, 999999, 0, 999999, 
		    999999, 50, 999999, 
		    50, 50, 99));
		for(Race race : races) {
			Frame.print(race.name + ", ");
		}
		Frame.println();
	}
	public Race getrace(String ra) {
		for(Race r : races) {
			if(r.name.equals(ra)) {
				return r;
			}
		}
		return null;
	}
//	public String converttosave(String s) {
//		
//	}
}
