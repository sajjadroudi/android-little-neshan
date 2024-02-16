package ir.roudi.littleneshan.ui.navigation;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import ir.roudi.littleneshan.data.repository.location.LocationRepository;
import ir.roudi.littleneshan.data.repository.navigation.NavigationRepository;

@HiltViewModel
public class NavigationViewModel extends ViewModel {

    private final LocationRepository locationRepository;
    private final NavigationRepository navigationRepository;

    @Inject
    public NavigationViewModel(
            LocationRepository locationRepository,
            NavigationRepository navigationRepository
    ) {
        this.locationRepository = locationRepository;
        this.navigationRepository = navigationRepository;
    }
}
