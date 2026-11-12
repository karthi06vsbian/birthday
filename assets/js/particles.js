/**
 * Romantic Particle Systems:
 * - Floating soft petals & golden stardust
 * - Confetti & celebration burst
 * - Interactive heart floaters
 */
class RomanticParticles {
  constructor(canvasId) {
    this.canvas = document.getElementById(canvasId);
    if (!this.canvas) return;
    this.ctx = this.canvas.getContext('2d');
    this.particles = [];
    this.sparkles = [];
    this.confetti = [];
    this.width = window.innerWidth;
    this.height = window.innerHeight;
    this.running = true;

    this.resize();
    window.addEventListener('resize', () => this.resize());
    this.initPetals();
    this.initSparkles();
    this.loop();
  }

  resize() {
    this.width = window.innerWidth;
    this.height = window.innerHeight;
    this.canvas.width = this.width;
    this.canvas.height = this.height;
  }

  initPetals() {
    const count = Math.min(28, Math.floor(this.width / 45));
    this.particles = [];
    for (let i = 0; i < count; i++) {
      this.particles.push({
        x: Math.random() * this.width,
        y: Math.random() * this.height,
        size: Math.random() * 8 + 6,
        speedY: Math.random() * 0.8 + 0.4,
        speedX: Math.sin(Math.random() * Math.PI) * 0.7,
        rotation: Math.random() * 360,
        rotationSpeed: (Math.random() - 0.5) * 1.5,
        color: ['#f472b6', '#fb7185', '#fda4af', '#fecdd3', '#fbcfe8'][Math.floor(Math.random() * 5)],
        opacity: Math.random() * 0.45 + 0.25,
        swing: Math.random() * 2 + 1,
        swingOffset: Math.random() * Math.PI * 2
      });
    }
  }

  initSparkles() {
    const count = 35;
    this.sparkles = [];
    for (let i = 0; i < count; i++) {
      this.sparkles.push({
        x: Math.random() * this.width,
        y: Math.random() * this.height,
        radius: Math.random() * 1.6 + 0.5,
        alpha: Math.random() * 0.7 + 0.2,
        alphaSpeed: (Math.random() * 0.02 + 0.005) * (Math.random() > 0.5 ? 1 : -1)
      });
    }
  }

  burstConfetti(originX, originY) {
    const x = originX || this.width / 2;
    const y = originY || this.height / 2;
    const colors = ['#f43f5e', '#ec4899', '#d946ef', '#f59e0b', '#fbbf24', '#ffffff', '#a855f7'];

    for (let i = 0; i < 90; i++) {
      const angle = Math.random() * Math.PI * 2;
      const velocity = Math.random() * 9 + 4;
      this.confetti.push({
        x: x,
        y: y,
        vx: Math.cos(angle) * velocity,
        vy: Math.sin(angle) * velocity - 3,
        size: Math.random() * 7 + 4,
        color: colors[Math.floor(Math.random() * colors.length)],
        rotation: Math.random() * 360,
        rotSpeed: (Math.random() - 0.5) * 12,
        gravity: 0.22,
        alpha: 1,
        life: 120
      });
    }
  }

  spawnHeart(x, y) {
    const el = document.createElement('div');
    el.className = 'floating-heart-bubble';
    const hearts = ['❤️', '💖', '✨', '🌸', '💕', '🌷'];
    el.textContent = hearts[Math.floor(Math.random() * hearts.length)];
    el.style.left = `${x}px`;
    el.style.top = `${y}px`;
    document.body.appendChild(el);
    setTimeout(() => el.remove(), 2200);
  }

  loop() {
    if (!this.running) return;
    this.ctx.clearRect(0, 0, this.width, this.height);

    // Render Stardust / Sparkles
    for (let s of this.sparkles) {
      s.alpha += s.alphaSpeed;
      if (s.alpha > 0.85 || s.alpha < 0.15) {
        s.alphaSpeed = -s.alphaSpeed;
      }
      this.ctx.beginPath();
      this.ctx.arc(s.x, s.y, s.radius, 0, Math.PI * 2);
      this.ctx.fillStyle = `rgba(253, 224, 71, ${s.alpha})`;
      this.ctx.shadowBlur = 6;
      this.ctx.shadowColor = 'rgba(253, 224, 71, 0.6)';
      this.ctx.fill();
    }
    this.ctx.shadowBlur = 0;

    // Render Petals
    const time = Date.now() * 0.001;
    for (let p of this.particles) {
      p.y += p.speedY;
      p.x += Math.sin(time + p.swingOffset) * p.swing * 0.3 + p.speedX * 0.2;
      p.rotation += p.rotationSpeed;

      if (p.y > this.height + 20) {
        p.y = -20;
        p.x = Math.random() * this.width;
      }
      if (p.x > this.width + 20) p.x = -20;
      if (p.x < -20) p.x = this.width + 20;

      this.ctx.save();
      this.ctx.translate(p.x, p.y);
      this.ctx.rotate((p.rotation * Math.PI) / 180);
      this.ctx.globalAlpha = p.opacity;

      // Realistic velvety rose petal gradient
      const grad = this.ctx.createRadialGradient(0, p.size * 0.4, 1, 0, 0, p.size);
      grad.addColorStop(0, '#fda4af');
      grad.addColorStop(0.5, p.color);
      grad.addColorStop(1, '#db2777');
      this.ctx.fillStyle = grad;

      // Draw realistic curved petal shape
      this.ctx.beginPath();
      this.ctx.moveTo(0, -p.size * 0.4);
      this.ctx.bezierCurveTo(-p.size * 0.8, -p.size * 0.8, -p.size, p.size * 0.6, 0, p.size);
      this.ctx.bezierCurveTo(p.size, p.size * 0.6, p.size * 0.8, -p.size * 0.8, 0, -p.size * 0.4);
      this.ctx.fill();

      // Delicate petal center crease highlight
      this.ctx.beginPath();
      this.ctx.moveTo(0, -p.size * 0.2);
      this.ctx.lineTo(0, p.size * 0.7);
      this.ctx.strokeStyle = 'rgba(255, 255, 255, 0.25)';
      this.ctx.lineWidth = 0.8;
      this.ctx.stroke();

      this.ctx.restore();
    }

    // Render Confetti
    for (let i = this.confetti.length - 1; i >= 0; i--) {
      const c = this.confetti[i];
      c.x += c.vx;
      c.y += c.vy;
      c.vy += c.gravity;
      c.vx *= 0.98;
      c.rotation += c.rotSpeed;
      c.life -= 1;
      c.alpha = Math.max(0, c.life / 120);

      this.ctx.save();
      this.ctx.translate(c.x, c.y);
      this.ctx.rotate((c.rotation * Math.PI) / 180);
      this.ctx.globalAlpha = c.alpha;
      this.ctx.fillStyle = c.color;
      this.ctx.fillRect(-c.size / 2, -c.size / 2, c.size, c.size * 0.6);
      this.ctx.restore();

      if (c.life <= 0) {
        this.confetti.splice(i, 1);
      }
    }

    requestAnimationFrame(() => this.loop());
  }
}

window.RomanticParticles = RomanticParticles;
