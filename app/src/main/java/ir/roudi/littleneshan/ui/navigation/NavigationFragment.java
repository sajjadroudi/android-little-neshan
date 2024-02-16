package ir.roudi.littleneshan.ui.navigation;

import static androidx.navigation.fragment.FragmentKt.findNavController;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ir.roudi.littleneshan.databinding.FragmentNavigationBinding;

public class NavigationFragment extends Fragment {

    private FragmentNavigationBinding binding;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        var args = NavigationFragmentArgs.fromBundle(getArguments());

        setupMapSetting(args.getMapStyle());

        binding.stop.setOnClickListener(v -> {
            findNavController(this).navigateUp();
        });
    }

    private void setupMapSetting(int mapStyle) {
        binding.map.setMapStyle(mapStyle);
        binding.map.setTrafficEnabled(true);
        binding.map.setPoiEnabled(true);
        binding.map.setTilt(40f, 0.25f);
    }

}
