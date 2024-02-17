package ir.roudi.littleneshan.ui.navigation;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.hilt.navigation.HiltViewModelFactory;
import androidx.lifecycle.ViewModelProvider;

import ir.roudi.littleneshan.R;
import ir.roudi.littleneshan.databinding.FragmentNavigationBinding;

public class NavigationFragment extends Fragment {

    private FragmentNavigationBinding binding;
    private NavigationViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentNavigationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViewModel();

       var args = NavigationFragmentArgs.fromBundle(getArguments());
       viewModel.startNavigation(args.getStart(), args.getEnd());
    }

    private void setupViewModel() {
        var backStackEntry = findNavController(this)
                .getBackStackEntry(R.id.nav_main);

        var factory = HiltViewModelFactory.create(requireContext(), backStackEntry);

        viewModel = new ViewModelProvider(backStackEntry, factory)
                .get(NavigationViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        var args = NavigationFragmentArgs.fromBundle(getArguments());

        setupMapSetting(args.getMapStyle());

        binding.stop.setOnClickListener(v -> {
            findNavController(this).navigateUp();
        });

        viewModel.direction.observe(getViewLifecycleOwner(), direction -> {
            if(direction == null)
                return;

            if(direction.getSteps() == null || direction.getSteps().isEmpty())
                return;

            var nextStep = direction.getSteps().get(0);
            var text =  "بعدی: " + nextStep.getName() + "\n" + nextStep.getDistance().getText() + " دیگر " + nextStep.getInstruction();
            binding.address.setText(text);
        });
    }

    private void setupMapSetting(int mapStyle) {
        binding.map.setMapStyle(mapStyle);
        binding.map.setTrafficEnabled(true);
        binding.map.setPoiEnabled(true);
        binding.map.setTilt(40f, 0.25f);
    }

}
