/**
 * Madhusree 20th Birthday - Image Asset Management System
 * Preserves full original image resolution and provides structured metadata.
 */

const MADHUSREE_IMAGE_ASSETS = [
  {
    id: "img-yellow-saree",
    title: "மஞ்சள் நிறத்தில் மாலை நிலவாய்",
    subtitle: "In silence and grace, uniquely remembered",
    category: "portrait",
    categoryLabel: "Portraits",
    src: "assets/images/madhusree-yellow-saree.jpg",
    highRes: "assets/images/madhusree-yellow-saree.jpg",
    orientation: "portrait",
    date: "A Cherished Moment",
    tag: "தனித்துவம்",
    note: "மௌனத்திலும் ஒரு கவிதையாய்... என் நினைவில் என்றும் தனித்துவமாய்.",
    aspect: "9/16"
  },
  {
    id: "img-portrait-main",
    title: "Pure Elegance & Gentle Smile",
    subtitle: "Natural warmth captured in daylight",
    category: "portrait",
    categoryLabel: "Portraits",
    src: "assets/images/madhusree-portrait-elegance.jpg",
    highRes: "assets/images/madhusree-portrait-elegance.jpg",
    orientation: "portrait",
    date: "13 November 2026",
    tag: "Spotlight",
    note: "Unna paatha eppovume oru happy feeling varum.",
    aspect: "3/4"
  },
  {
    id: "img-scrapbook-pink",
    title: "Vintage Scrapbook & 20th Birthday Keepsake",
    subtitle: "A collage of smiles, colors, and memories",
    category: "collage",
    categoryLabel: "Special Collages",
    src: "assets/images/madhusree-scrapbook-vintage.jpg",
    highRes: "assets/images/madhusree-scrapbook-vintage.jpg",
    orientation: "landscape",
    date: "13.11.2006 – 13.11.2026",
    tag: "Milestone",
    note: "Indha birthday unakku marakka mudiyadha oru memory ah irukkanum.",
    aspect: "16/10"
  },
  {
    id: "img-starlight-scrapbook",
    title: "Starlight & Lavender Memories",
    subtitle: "Under soft lights, crowned with joy",
    category: "collage",
    categoryLabel: "Special Collages",
    src: "assets/images/madhusree-starlight-scrapbook.jpg",
    highRes: "assets/images/madhusree-starlight-scrapbook.jpg",
    orientation: "landscape",
    date: "20th Birthday",
    tag: "Cherished",
    note: "Un presence-la oru thani magizhchi.",
    aspect: "16/10"
  },
  {
    id: "img-floral-celebration",
    title: "Blossoms of Grace & Laughter",
    subtitle: "Fresh pink florals framing your beautiful journey",
    category: "collage",
    categoryLabel: "Special Collages",
    src: "assets/images/madhusree-floral-celebration.jpg",
    highRes: "assets/images/madhusree-floral-celebration.jpg",
    orientation: "landscape",
    date: "Celebration",
    tag: "Radiance",
    note: "Indha ulagathil nee oru azhagana varam.",
    aspect: "16/10"
  },
  {
    id: "img-moments-mosaic",
    title: "Colors, Art & Every Special Expression",
    subtitle: "From traditional grace to candid laughter",
    category: "collage",
    categoryLabel: "Special Collages",
    src: "assets/images/madhusree-moments-mosaic.jpg",
    highRes: "assets/images/madhusree-moments-mosaic.jpg",
    orientation: "landscape",
    date: "Treasured Moments",
    tag: "Mosaic",
    note: "Un chinna chinna reactions unnai romba special aakkudhu.",
    aspect: "16/10"
  },
  {
    id: "img-polaroid-bird",
    title: "A Heart full of Kindness",
    subtitle: "Gentle friendship with nature",
    category: "polaroid",
    categoryLabel: "Polaroids",
    src: "assets/images/polaroids/polaroid-bird.jpg",
    highRes: "assets/images/polaroids/polaroid-bird.jpg",
    orientation: "portrait",
    date: "Sweet Moments",
    tag: "Gentle Spirit",
    note: "Unoda character unnai unmaiyave special aakkudhu.",
    aspect: "1/1"
  },
  {
    id: "img-polaroid-saree",
    title: "Timeless Traditional Grace",
    subtitle: "Elegance in silk and radiant modesty",
    category: "polaroid",
    categoryLabel: "Polaroids",
    src: "assets/images/polaroids/polaroid-saree.jpg",
    highRes: "assets/images/polaroids/polaroid-saree.jpg",
    orientation: "portrait",
    date: "Festive Vibes",
    tag: "Tradition",
    note: "Un paarvaiyil oru azhagu.",
    aspect: "1/1"
  },
  {
    id: "img-polaroid-lake",
    title: "Peaceful Serenity by the Water",
    subtitle: "Calm breezes and thoughtful skies",
    category: "polaroid",
    categoryLabel: "Polaroids",
    src: "assets/images/polaroids/polaroid-lake.jpg",
    highRes: "assets/images/polaroids/polaroid-lake.jpg",
    orientation: "portrait",
    date: "Reflections",
    tag: "Serenity",
    note: "Un life la nee nenacha ellame nadakkanum.",
    aspect: "1/1"
  },
  {
    id: "img-polaroid-music",
    title: "Rhythm & Joyful Expression",
    subtitle: "Vibrant art celebrating your vibrant self",
    category: "polaroid",
    categoryLabel: "Polaroids",
    src: "assets/images/polaroids/polaroid-music-wall.jpg",
    highRes: "assets/images/polaroids/polaroid-music-wall.jpg",
    orientation: "portrait",
    date: "Urban Vibes",
    tag: "Art & Melody",
    note: "Un pechil oru inimai.",
    aspect: "1/1"
  }
];

window.MadhusreeGallery = {
  assets: MADHUSREE_IMAGE_ASSETS,

  getAll() {
    return this.assets;
  },

  getById(id) {
    return this.assets.find(item => item.id === id);
  },

  getByCategory(cat) {
    if (!cat || cat === 'all') return this.assets;
    return this.assets.filter(item => item.category === cat);
  }
};
