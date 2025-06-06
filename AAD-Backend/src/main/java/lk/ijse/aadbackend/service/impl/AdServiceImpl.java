package lk.ijse.aadbackend.service.impl;

import lk.ijse.aadbackend.dto.AdDTO;
import lk.ijse.aadbackend.dto.AdSearchRequestDTO;
import lk.ijse.aadbackend.entity.Ad;
import lk.ijse.aadbackend.entity.Category;
import lk.ijse.aadbackend.entity.Location;
import lk.ijse.aadbackend.entity.User;
import lk.ijse.aadbackend.enums.Status;
import lk.ijse.aadbackend.repo.AdRepository;
import lk.ijse.aadbackend.repo.CategoryRepository;
import lk.ijse.aadbackend.repo.LocationRepository;
import lk.ijse.aadbackend.repo.UserRepository;
import lk.ijse.aadbackend.service.AdService;
import lk.ijse.aadbackend.specification.AdSpecification;
import lk.ijse.aadbackend.util.VarList;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdServiceImpl implements AdService {

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    public int createAd(AdDTO adDTO, List<MultipartFile> imageFiles) {
        try {
            // Create new Ad instance
            Ad ad = new Ad();

            if (adDTO.getId() != null) {
                ad.setId(adDTO.getId());
            }
            ad.setTitle(adDTO.getTitle());
            ad.setDescription(adDTO.getDescription());
            ad.setPrice(adDTO.getPrice());
            ad.setStatus(adDTO.getStatus());
            ad.setCreatedAt(LocalDateTime.now());
            ad.setUpdatedAt(LocalDateTime.now());

            // Fetch related entities
            User user = userRepository.findById(adDTO.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Category category = categoryRepository.findById(adDTO.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            Location location = locationRepository.findById(adDTO.getLocationId())
                    .orElseThrow(() -> new RuntimeException("Location not found"));

            ad.setUser(user);
            ad.setCategory(category);
            ad.setLocation(location);

            Ad savedAd = adRepository.save(ad);

            if (imageFiles != null && !imageFiles.isEmpty()) {
                StringBuilder imageNames = new StringBuilder();

                String uploadDir = System.getProperty("user.dir") + "/uploadImages/";
                File uploadFolder = new File(uploadDir);
                if (!uploadFolder.exists() && !uploadFolder.mkdirs()) {
                    throw new RuntimeException("Failed to create upload directory!");
                }

                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                        File destinationFile = new File(uploadDir + uniqueFileName);

                        System.out.println("Saving file to: " + destinationFile.getAbsolutePath());

                        file.transferTo(destinationFile);

                        if (imageNames.length() > 0) {
                            imageNames.append(",");
                        }
                        imageNames.append(uniqueFileName);
                    }
                }

                savedAd.setImages(imageNames.toString());
                adRepository.save(savedAd);
            }

            return VarList.Created;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while saving ad: " + e.getMessage(), e);
        }
    }


    @Override
    public int deleteAd(UUID adId) {
        try {
            Ad ad = adRepository.findById(adId)
                    .orElseThrow(() -> new RuntimeException("Ad not found"));

            ad.setStatus(Status.DELETED.toString());
            adRepository.save(ad);

            return VarList.Created;
        } catch (Exception e) {
            e.printStackTrace();
            return VarList.Internal_Server_Error;
        }
    }


    @Override
    public List<AdDTO> getAllActiveAds() {
        List<Ad> activeAds = adRepository.findByStatusOrderByCreatedAtDesc("ACTIVE");

        return activeAds.stream().map(ad -> {
            AdDTO adDTO = modelMapper.map(ad, AdDTO.class);

            // Convert the comma-separated image string into a list
            List<String> imageUrls = new ArrayList<>();
            if (ad.getImages() != null && !ad.getImages().isEmpty()) {
                imageUrls = Arrays.stream(ad.getImages().split(","))
                        .map(fileName -> "http://localhost:8082/uploadImages/" + fileName.trim())
                        .toList();
            }

            adDTO.setImageUrls(imageUrls);
//            System.out.println("Ad Details:- " + adDTO);

            return adDTO;
        }).toList();
    }


    @Override
    public List<AdDTO> getAdsByUserId(UUID userId) {
        List<Ad> ads = adRepository.findByUserId(userId);
        return ads.stream().map(ad -> {
            AdDTO adDTO = modelMapper.map(ad, AdDTO.class);

            // Convert the comma-separated image string into a list
            List<String> imageUrls = new ArrayList<>();
            if (ad.getImages() != null && !ad.getImages().isEmpty()) {
                imageUrls = Arrays.stream(ad.getImages().split(","))
                        .map(fileName -> "http://localhost:8082/uploadImages/" + fileName.trim())
                        .toList();
            }

            adDTO.setImageUrls(imageUrls);
//            System.out.println("Ad Details:- " + adDTO);

            return adDTO;
        }).toList();
    }

    @Override
    public AdDTO getAdDetailsByAdId(UUID adId) {
        Optional<Ad> adOptional = adRepository.findById(adId);

        if (adOptional.isPresent()) {
            Ad ad = adOptional.get();
            AdDTO adDTO = modelMapper.map(ad, AdDTO.class);

            // Convert the comma-separated image string into a list
            List<String> imageUrls = new ArrayList<>();
            if (ad.getImages() != null && !ad.getImages().isEmpty()) {
                imageUrls = Arrays.stream(ad.getImages().split(","))
                        .map(fileName -> "http://localhost:8082/uploadImages/" + fileName.trim())
                        .toList();
            }

            // Fetch category name
            if (ad.getCategory() != null) {
                adDTO.setCategoryName(ad.getCategory().getName());
            }

            // Fetch location name with parent location
            if (ad.getLocation() != null) {
                StringBuilder locationName = new StringBuilder(ad.getLocation().getName());

                if (ad.getLocation().getParentLocation() != null) {
                    locationName.insert(0, ad.getLocation().getParentLocation().getName() + ", ");
                }

                adDTO.setLocationName(locationName.toString());
            }


            adDTO.setImageUrls(imageUrls);
//            System.out.println("Ad Details: " + adDTO);

            return adDTO;
        }

        return null; // Or throw a custom exception if needed
    }





    public int updateAd(AdDTO adDTO, List<MultipartFile> newImages) throws IOException {
        Optional<Ad> optionalAd = adRepository.findById(adDTO.getId());
        if (!optionalAd.isPresent()) return VarList.Not_Found;

        Ad ad = optionalAd.get();

        // 1. Get current image list from DB
        List<String> dbImageList = new ArrayList<>();
        if (ad.getImages() != null && !ad.getImages().isEmpty()) {
            dbImageList = new ArrayList<>(Arrays.asList(ad.getImages().split(",")));
        }

        // 2. Get retained images sent from frontend
        List<String> retainedImages = adDTO.getExistingImageNames() != null ? adDTO.getExistingImageNames() : new ArrayList<>();

        // 3. Identify deleted images
        List<String> imagesToDelete = dbImageList.stream()
                .filter(img -> !retainedImages.contains(img))
                .collect(Collectors.toList());

        // 4. Delete removed images from uploadImages/ folder
        for (String imgName : imagesToDelete) {
            File file = new File("uploadImages/" + imgName);
            if (file.exists()) {
                file.delete();
            }
        }

        // 5. Start final image list with retained ones
        List<String> finalImageList = new ArrayList<>(retainedImages);

        // 6. Save new uploaded images
        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile file : newImages) {
                String uniqueFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                Path filePath = Paths.get("uploadImages", uniqueFileName);
                Files.write(filePath, file.getBytes());

                finalImageList.add(uniqueFileName);
            }
        }

        // 7. Update ad entity fields
        ad.setImages(String.join(",", finalImageList));
        ad.setTitle(adDTO.getTitle());
        ad.setDescription(adDTO.getDescription());
        ad.setPrice(adDTO.getPrice());
        ad.setStatus(adDTO.getStatus());
        ad.setUser(userRepository.findById(adDTO.getUserId()).orElse(null));
        ad.setLocation(locationRepository.findById(adDTO.getLocationId()).orElse(null));
        ad.setCategory(categoryRepository.findById(adDTO.getCategoryId()).orElse(null));

        // 8. Save and return
        adRepository.save(ad);
        return VarList.Created;
    }


    @Override
    public int countActiveAdsByParentCategory(UUID parentCategoryId) {
        return adRepository.countActiveAdsByParentCategory(parentCategoryId);
    }




    @Override
    public List<AdDTO> filterAds(UUID subcategoryId, UUID districtId, UUID cityId, UUID parentCategoryId) {
        List<Ad> ads;
        String status = "ACTIVE";

        // First, handle parent category cases
        if (parentCategoryId != null) {
            if (districtId != null && cityId != null) {
                // Parent category + district + city
                ads = adRepository.findByStatusAndCategoryParentCategoryIdAndLocationParentLocationIdAndLocationId(
                        status, parentCategoryId, districtId, cityId);
            } else if (districtId != null) {
                // Parent category + district
                ads = adRepository.findByStatusAndCategoryParentCategoryIdAndLocationParentLocationId(
                        status, parentCategoryId, districtId);
            } else if (cityId != null) {
                // Parent category + city
                ads = adRepository.findByStatusAndCategoryParentCategoryIdAndLocationId(
                        status, parentCategoryId, cityId);
            } else {
                // Only parent category
                ads = adRepository.findByStatusAndCategoryParentCategoryId(status, parentCategoryId);
            }
        }


        if (subcategoryId != null && districtId != null && cityId != null) {
            // All filters applied
            ads = adRepository.findByStatusAndCategoryIdAndLocationParentLocationIdAndLocationId(
                    status, subcategoryId, districtId, cityId);
        } else if (subcategoryId != null && districtId != null) {
            // Category and district
            ads = adRepository.findByStatusAndCategoryIdAndLocationParentLocationId(
                    status, subcategoryId, districtId);
        } else if (subcategoryId != null && cityId != null) {
            // Category and city
            ads = adRepository.findByStatusAndCategoryIdAndLocationId(
                    status, subcategoryId, cityId);
        } else if (districtId != null && cityId != null) {
            // District and city
            ads = adRepository.findByStatusAndLocationParentLocationIdAndLocationId(
                    status, districtId, cityId);
        } else if (subcategoryId != null) {
            // Only category
            ads = adRepository.findByStatusAndCategoryId(status, subcategoryId);
        } else if (districtId != null) {
            // Only district
            ads = adRepository.findByStatusAndLocationParentLocationId(status, districtId);
        } else if (cityId != null) {
            // Only city
            ads = adRepository.findByStatusAndLocationId(status, cityId);
        } else {
            // No filters
            ads = adRepository.findByStatus(status);
        }

        return ads.stream()
                .map(ad -> {
                    AdDTO dto = modelMapper.map(ad, AdDTO.class);
                    // Manually set userName and locationName
                    dto.setUserName(ad.getUser().getName());
                    dto.setLocationName(ad.getLocation().getName());

                    // Set parent location name if available
                    if (ad.getLocation().getParentLocation() != null) {
                        dto.setParentLocationName(ad.getLocation().getParentLocation().getName());
                    }

                    // Set category name
                    dto.setCategoryName(ad.getCategory().getName());

                    // Convert images string to List<String> of URLs
                    List<String> imageUrls = new ArrayList<>();
                    if (ad.getImages() != null && !ad.getImages().isEmpty()) {
                        String[] imageFilenames = ad.getImages().split(",");
                        for (String filename : imageFilenames) {
                            imageUrls.add("http://localhost:8082/uploadImages/" + filename.trim());
                        }
                    }
                    dto.setImageUrls(imageUrls);
                    return dto;
                })
                .collect(Collectors.toList());
    }








    @Override
    public List<AdDTO> searchAds(AdSearchRequestDTO searchRequest) {
        List<Ad> ads = adRepository.searchAds(
                searchRequest.getKeyword(),
                searchRequest.getCategoryId(),
                searchRequest.getDistrictId(),
                searchRequest.getCityId()
        );

        return ads.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private AdDTO convertToDTO(Ad ad) {
        AdDTO dto = new AdDTO();
        dto.setId(ad.getId());
        dto.setTitle(ad.getTitle());
        dto.setDescription(ad.getDescription());
        dto.setPrice(ad.getPrice());
        dto.setStatus(ad.getStatus());
        dto.setUserId(ad.getUser().getId());
        dto.setUserName(ad.getUser().getName());
        dto.setUserPhone(ad.getUser().getPhone());
        dto.setCategoryId(ad.getCategory().getId());
        dto.setCategoryName(ad.getCategory().getName());
        dto.setLocationId(ad.getLocation().getId());
        dto.setLocationName(ad.getLocation().getName());
        dto.setParentLocationName(ad.getLocation().getParentLocation().getName());
        dto.setCreatedAt(ad.getCreatedAt().toString());

        // handle image URLs
        if (ad.getImages() != null && !ad.getImages().isEmpty()) {
            dto.setImageUrls(List.of(ad.getImages().split(",")));
        }

        return dto;
    }






//
//    private List<String> saveImages(List<MultipartFile> imageFiles) throws IOException {
//        if (imageFiles == null || imageFiles.isEmpty()) return new ArrayList<>();
//
//        List<String> fileNames = new ArrayList<>();
//
//
//        String uploadDir = System.getProperty("user.dir") + "/uploadImages/";
//        for (MultipartFile file : imageFiles) {
//            if (!file.isEmpty()) {
//                String uniqueName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//                File destinationFile = new File(uploadDir + File.separator + uniqueName);
//                file.transferTo(destinationFile);
//                fileNames.add(uniqueName);
//            }
//        }
//        return fileNames;
//    }



}
