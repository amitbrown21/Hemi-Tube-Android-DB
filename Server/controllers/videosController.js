const videosServices = require("../services/videosServices");

const videosController = {
  getAllVideos: async (req, res) => {
    try {
      const videos = await videosServices.getVideos();
      res.json(videos);
    } catch (error) {
      console.error("Error getting all videos:", error);
      res.status(500).json({ message: error.message });
    }
  },

  getVideoById: async (req, res) => {
    try {
      const video = await videosServices.getVideoById(req.params.pid);
      if (!video) {
        return res.status(404).json({ message: "Video not found" });
      }
      res.json(video);
    } catch (error) {
      console.error("Error getting video by ID:", error);
      res.status(500).json({ message: error.message });
    }
  },

  createVideo: async (req, res) => {
    console.log("createVideo called");
    console.log("Request body:", req.body);
    console.log("Request file:", req.file);
    
    try {
      if (!req.file) {
        console.log("No video file uploaded");
        return res.status(400).json({ message: "No video file uploaded" });
      }

      const userId = req.params.id; // Get userId from route parameter
      const { title, description, thumbnail, duration } = req.body;

      console.log("Creating video with data:", { userId, title, description, thumbnail, duration });

      const newVideo = await videosServices.createVideo(userId, {
        title,
        description,
        filePath: `/uploads/videos/${req.file.filename}`,
        thumbnail,
        duration,
        owner: userId,
      });

      console.log("Video created:", newVideo);
      res.status(201).json(newVideo);
    } catch (error) {
      console.error("Error creating video:", error);
      res.status(400).json({ message: error.message });
    }
  },

  updateVideo: async (req, res) => {
    try {
      const userId = req.user.userId;
      const videoId = req.params.pid;

      console.log(`Updating video ${videoId} for user ${userId}`);

      const video = await videosServices.getVideoById(videoId);
      if (!video) {
        console.log(`Video ${videoId} not found`);
        return res.status(404).json({ message: "Video not found" });
      }

      if (video.owner.toString() !== userId) {
        console.log(`User ${userId} not authorized to edit video ${videoId}`);
        return res.status(403).json({ message: "You are not authorized to edit this video" });
      }

      const updatedVideo = await videosServices.updateVideo(videoId, req.body);
      console.log("Video updated:", updatedVideo);
      res.json(updatedVideo);
    } catch (error) {
      console.error("Error updating video:", error);
      res.status(400).json({ message: error.message });
    }
  },

  deleteVideo: async (req, res) => {
    try {
      const userId = req.user.userId;
      const videoId = req.params.pid;

      console.log(`Deleting video ${videoId} for user ${userId}`);

      const video = await videosServices.getVideoById(videoId);
      if (!video) {
        console.log(`Video ${videoId} not found`);
        return res.status(404).json({ message: "Video not found" });
      }

      if (video.owner.toString() !== userId) {
        console.log(`User ${userId} not authorized to delete video ${videoId}`);
        return res.status(403).json({ message: "You are not authorized to delete this video" });
      }

      await videosServices.deleteVideo(videoId);
      console.log(`Video ${videoId} deleted successfully`);
      res.status(204).send();
    } catch (error) {
      console.error("Error deleting video:", error);
      res.status(400).json({ message: error.message });
    }
  },

  incrementViews: async (req, res) => {
    try {
      const videoId = req.params.pid;
      console.log(`Incrementing views for video ${videoId}`);
      const updatedVideo = await videosServices.incrementViews(videoId);
      res.json(updatedVideo);
    } catch (error) {
      console.error("Error incrementing views:", error);
      res.status(500).json({ message: error.message });
    }
  },

  incrementLikes: async (req, res) => {
    try {
      const videoId = req.params.pid;
      console.log(`Incrementing likes for video ${videoId}`);
      const updatedVideo = await videosServices.incrementLikes(videoId);
      res.json(updatedVideo);
    } catch (error) {
      console.error("Error incrementing likes:", error);
      res.status(500).json({ message: error.message });
    }
  },

  decrementLikes: async (req, res) => {
    try {
      const videoId = req.params.pid;
      console.log(`Decrementing likes for video ${videoId}`);
      const updatedVideo = await videosServices.decrementLikes(videoId);
      res.json(updatedVideo);
    } catch (error) {
      console.error("Error decrementing likes:", error);
      res.status(500).json({ message: error.message });
    }
  },

  incrementDislikes: async (req, res) => {
    try {
      const videoId = req.params.pid;
      console.log(`Incrementing dislikes for video ${videoId}`);
      const updatedVideo = await videosServices.incrementDislikes(videoId);
      res.json(updatedVideo);
    } catch (error) {
      console.error("Error incrementing dislikes:", error);
      res.status(500).json({ message: error.message });
    }
  },

  decrementDislikes: async (req, res) => {
    try {
      const videoId = req.params.pid;
      console.log(`Decrementing dislikes for video ${videoId}`);
      const updatedVideo = await videosServices.decrementDislikes(videoId);
      res.json(updatedVideo);
    } catch (error) {
      console.error("Error decrementing dislikes:", error);
      res.status(500).json({ message: error.message });
    }
  },

  getAllVideosWithTopAndRandom: async (req, res) => {
    try {
      console.log("Getting all videos with top and random");
      const videos = await videosServices.getVideosWithTopAndRandom();
      res.json(videos);
    } catch (error) {
      console.error("Error getting all videos with top and random:", error);
      res.status(500).json({ message: error.message });
    }
  },

  getAllVideos: async (req, res) => {
    try {
      console.log("Getting all videos");
      const videos = await videosServices.getVideos();
      res.json(videos);
    } catch (error) {
      console.error("Error getting all videos:", error);
      res.status(500).json({ message: error.message });
    }
  },
};

module.exports = videosController;