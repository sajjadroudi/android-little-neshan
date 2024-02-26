## Bug
- Improve path polyline drawing to be drawn from user location to destination.

## Improvement
- Clean up classes and refactor codes.
- Define string resources.
- Use actions for navigation.
- Use parcelable instead of serializable.
- Add "return to the path" feature to the navigation screen.
- Advance navigation.
  - Like checking that the user is getting backward or he/she are far from the path.
- Check di components.
- Improve focus location.
  - User marker should be bottom instead of center of the screen.
  - Just like Neshan.
- Load last location when GPS is turned off.
  - Need to cache last location in shared preferences.
- Compatibility with Android 14.
- Define UI model.

## DONE
- Improve location precision in navigation screen.
- Stop location updates when needed.
- Initial focus on user location in the navigation screen.
- Add destination marker to the navigation screen.
- Improve camera bounds to cover all the path from source to destination.
- Update distance and duration automatically in the navigation screen.
- Improve guide in the navigation screen.
- Add direction of the car when navigating.
- Handle landscape.
