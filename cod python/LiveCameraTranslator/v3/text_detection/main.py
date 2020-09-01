from v3.text_detection import findContour, image_preprocecing


def get_proposed_regions(
        image,
        applyBiltaralFilter,
        bilateralFilter_d,
        bilateralFilter_sigmaColor,
        bilateralFilter_sigmaSpace,
        applyGaussianBlur,
        use_otsu,
        use_adaptive_mean,
        use_adaptive_gaussian,
        min_area_threshold,
        max_area_threshold,
        min_proportion_treshold,
        max_proportion_treshold,
        show_steps,
        ):

    time1 = time2 = time3 = 0

    preprocessed_image,time1 = image_preprocecing.preprocess(image,
                                                             applyBiltaralFilter,
                                                             bilateralFilter_d,
                                                             bilateralFilter_sigmaColor,
                                                             bilateralFilter_sigmaSpace,
                                                             applyGaussianBlur,
                                                             show_steps)

    regions,binary_image,time2 = findContour.get_proposed_regions(preprocessed_image,
                                                                  image,
                                                                  use_otsu,
                                                                  use_adaptive_mean,
                                                                  use_adaptive_gaussian,
                                                                  min_area_threshold,
                                                                  max_area_threshold,
                                                                  min_proportion_treshold,
                                                                  max_proportion_treshold,
                                                                  show_steps)

    return regions,binary_image,time1,time2
