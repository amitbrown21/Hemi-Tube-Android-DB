const express = require("express");
const router = express.Router();
const multer = require('multer');
const path = require('path');
const videosController = require("../controllers/videosController");
const authMiddleware = require("../middleware/auth");

const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, 'uploads/videos')
  },
  filename: function (req, file, cb) {
    cb(null, Date.now() + path.extname(file.originalname))
  }
})

const upload = multer({ 
  storage: storage,
  fileFilter: function (req, file, cb) {
    if (file.mimetype.startsWith('video/')) {
      cb(null, true)
    } else {
      cb(new Error('Not a video file!'), false)
    }
  },
  limits: {
    fileSize: 100 * 1024 * 1024 // 100 MB limit
  }
})

router.get("/", videosController.getAllVideosWithTopAndRandom);
router.post("/:pid/incrementViews", videosController.incrementViews);
router.post("/:pid/incrementLikes", videosController.incrementLikes);
router.post("/:pid/decrementLikes", videosController.decrementLikes);
router.post("/:pid/incrementDislikes", videosController.incrementDislikes);
router.post("/:pid/decrementDislikes", videosController.decrementDislikes);
router.get("/all", videosController.getAllVideos);
router.get("/:pid", videosController.getVideoById);


module.exports = router;