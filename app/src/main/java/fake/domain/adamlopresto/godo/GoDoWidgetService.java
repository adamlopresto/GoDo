package fake.domain.adamlopresto.godo;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class GoDoWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GoDoViewsFactory(this.getApplicationContext());
    }
}
