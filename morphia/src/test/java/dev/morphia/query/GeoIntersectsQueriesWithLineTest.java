package dev.morphia.query;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.geojson.GeometryCollection;
import com.mongodb.client.model.geojson.LineString;
import com.mongodb.client.model.geojson.MultiPoint;
import com.mongodb.client.model.geojson.MultiPolygon;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Polygon;
import com.mongodb.client.model.geojson.PolygonCoordinates;
import com.mongodb.client.model.geojson.Position;
import dev.morphia.TestBase;
import dev.morphia.geo.AllTheThings;
import dev.morphia.geo.Area;
import dev.morphia.geo.City;
import dev.morphia.geo.Regions;
import dev.morphia.geo.Route;
import org.junit.Before;
import org.junit.Test;

import static java.util.List.of;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

@SuppressWarnings("unchecked")
public class GeoIntersectsQueriesWithLineTest extends TestBase {
    @Override
    @Before
    public void setUp() {
        // this whole test class is designed for "modern" geo queries
        checkMinServerVersion(2.4);
        super.setUp();
    }

    @Test
    public void shouldFindAPointThatLiesOnTheQueryLine() {
        // given
        LineString spanishLine = new LineString(of(
            new Position(37.40759155713022, -5.964911067858338),
            new Position(37.3753708, -5.9550582)));
        City manchester = new City("Manchester", new Point(new Position(53.4722454, -2.2235922)));
        getDs().save(manchester);
        City london = new City("London", new Point(new Position(51.5286416, -0.1015987)));
        getDs().save(london);
        City sevilla = new City("Sevilla", new Point(new Position(37.3753708, -5.9550582)));
        getDs().save(sevilla);

        getDs().ensureIndexes();

        // when
        MongoCursor<City> matchingCity = getDs().find(City.class)
                                                .field("location")
                                                .intersects(spanishLine)
                                                .execute();

        // then
        assertThat(matchingCity.next(), is(sevilla));
        assertFalse(matchingCity.hasNext());
    }

    @Test
    public void shouldFindAreasThatALineCrosses() {
        // given
        Area sevilla = new Area("Spain",
            new Polygon(of(
                new Position(37.40759155713022, -5.964911067858338),
                new Position(37.40341208875179, -5.9643941558897495),
                new Position(37.40297396667302, -5.970452763140202),
                new Position(37.40759155713022, -5.964911067858338))));
        getDs().save(sevilla);
        Area newYork = new Area("New York",
            new Polygon(of(
                new Position(40.75981395319104, -73.98302106186748),
                new Position(40.7636824529618, -73.98049869574606),
                new Position(40.76962974853814, -73.97964206524193),
                new Position(40.75981395319104, -73.98302106186748))));
        getDs().save(newYork);
        Area london = new Area("London",
            new Polygon(of(
                new Position(51.507780365645885, -0.21786745637655258),
                new Position(51.50802478194237, -0.21474729292094707),
                new Position(51.5086863655597, -0.20895397290587425),
                new Position(51.507780365645885, -0.21786745637655258))));
        getDs().save(london);
        getDs().ensureIndexes();

        // when
        MongoCursor<Area> areaContainingPoint = getDs().find(Area.class)
                                                       .field("area")
                                                       .intersects(new LineString(of(
                                                           new Position(37.4056048, -5.9666089),
                                                           new Position(37.404497, -5.9640557))))
                                                       .execute();

        // then
        assertThat(areaContainingPoint.next(), is(sevilla));
        assertFalse(areaContainingPoint.hasNext());
    }

    @Test
    public void shouldFindGeometryCollectionsWhereTheGivenPointIntersectsWithOneOfTheEntities() {
        checkMinServerVersion(2.6);
        // given
        AllTheThings sevilla = new AllTheThings("Spain", new GeometryCollection(of(
            new MultiPoint(of(
                new Position(37.40759155713022, -5.964911067858338),
                new Position(37.40341208875179, -5.9643941558897495),
                new Position(37.40297396667302, -5.970452763140202))),
            new Polygon(of(
                new Position(37.40759155713022, -5.964911067858338),
                new Position(37.40341208875179, -5.9643941558897495),
                new Position(37.40297396667302, -5.970452763140202),
                new Position(37.40759155713022, -5.964911067858338))),
            new Polygon(of(
                new Position(37.38744598813355, -6.001141928136349),
                new Position(37.385990973562, -6.002588979899883),
                new Position(37.386126928031445, -6.002463921904564),
                new Position(37.38744598813355, -6.001141928136349))))));
        getDs().save(sevilla);

        // insert something that's not a geocollection
        Regions usa = new Regions("US", new MultiPolygon(of(
            new PolygonCoordinates(of(
                new Position(40.75981395319104, -73.98302106186748),
                new Position(40.7636824529618, -73.98049869574606),
                new Position(40.76962974853814, -73.97964206524193),
                new Position(40.75981395319104, -73.98302106186748))),
            new PolygonCoordinates(of(
                new Position(28.326568258926272, -81.60542246885598),
                new Position(28.327541397884488, -81.6022228449583),
                new Position(28.32950334995985, -81.60564735531807),
                new Position(28.326568258926272, -81.60542246885598))))));
        getDs().save(usa);

        AllTheThings london = new AllTheThings("London", new GeometryCollection(of(
            new Point(new Position(53.4722454, -2.2235922)),
            new LineString(of(
                new Position(51.507780365645885, -0.21786745637655258),
                new Position(51.50802478194237, -0.21474729292094707),
                new Position(51.5086863655597, -0.20895397290587425))),
            new Polygon(of(
                new Position(51.498216362670064, 0.0074849557131528854),
                new Position(51.49176875129342, 0.01821178011596203),
                new Position(51.492886897176504, 0.05523204803466797),
                new Position(51.49393044412136, 0.06663135252892971),
                new Position(51.498216362670064, 0.0074849557131528854))))));
        getDs().save(london);
        getDs().ensureIndexes();

        // when
        MongoCursor<AllTheThings> everythingInTheUK = getDs().find(AllTheThings.class)
                                                             .field("everything")
                                                             .intersects(new LineString(of(
                                                                 new Position(37.4056048, -5.9666089),
                                                                 new Position(37.404497, -5.9640557))))
                                                             .execute();

        // then
        assertThat(everythingInTheUK.next(), is(sevilla));
        assertFalse(everythingInTheUK.hasNext());
    }

    @Test
    public void shouldFindRegionsThatALineCrosses() {
        checkMinServerVersion(2.6);
        // given
        Regions sevilla = new Regions("Spain", new MultiPolygon(of(
            new PolygonCoordinates(of(
                new Position(37.40759155713022, -5.964911067858338),
                new Position(37.40341208875179, -5.9643941558897495),
                new Position(37.40297396667302, -5.970452763140202),
                new Position(37.40759155713022, -5.964911067858338))),
            new PolygonCoordinates(of(
                new Position(37.38744598813355, -6.001141928136349),
                new Position(37.385990973562, -6.002588979899883),
                new Position(37.386126928031445, -6.002463921904564),
                new Position(37.38744598813355, -6.001141928136349))))));
        getDs().save(sevilla);

        Regions usa = new Regions("US", new MultiPolygon(of(
            new PolygonCoordinates(of(
                new Position(40.75981395319104, -73.98302106186748),
                new Position(40.7636824529618, -73.98049869574606),
                new Position(40.76962974853814, -73.97964206524193),
                new Position(40.75981395319104, -73.98302106186748))),
            new PolygonCoordinates(of(
                new Position(28.326568258926272, -81.60542246885598),
                new Position(28.327541397884488, -81.6022228449583),
                new Position(28.32950334995985, -81.60564735531807),
                new Position(28.326568258926272, -81.60542246885598))))));
        getDs().save(usa);

        Regions london = new Regions("London", new MultiPolygon(of(
            new PolygonCoordinates(of(
                new Position(51.507780365645885, -0.21786745637655258),
                new Position(51.50802478194237, -0.21474729292094707),
                new Position(51.5086863655597, -0.20895397290587425),
                new Position(51.507780365645885, -0.21786745637655258))),
            new PolygonCoordinates(of(
                new Position(51.498216362670064, 0.0074849557131528854),
                new Position(51.49176875129342, 0.01821178011596203),
                new Position(51.492886897176504, 0.05523204803466797),
                new Position(51.49393044412136, 0.06663135252892971),
                new Position(51.498216362670064, 0.0074849557131528854))))));
        getDs().save(london);
        getDs().ensureIndexes();

        // when
        MongoCursor<Regions> regionsInTheUK = getDs().find(Regions.class)
                                                     .field("regions")
                                                     .intersects(new LineString(of(
                                                         new Position(37.4056048, -5.9666089),
                                                         new Position(37.404497, -5.9640557))))
                                                     .execute();

        // then
        assertThat(regionsInTheUK.next(), is(sevilla));
        assertFalse(regionsInTheUK.hasNext());
    }

    @Test
    public void shouldFindRoutesThatALineCrosses() {
        // given
        Route sevilla = new Route("Spain", new LineString(of(
            new Position(37.4045286, -5.9642332),
            new Position(37.4061095, -5.9645765))));
        getDs().save(sevilla);

        Route newYork = new Route("New York", new LineString(of(
            new Position(40.75981395319104, -73.98302106186748),
            new Position(40.7636824529618, -73.98049869574606),
            new Position(40.76962974853814, -73.97964206524193))));
        getDs().save(newYork);

        Route london = new Route("London", new LineString(of(
            new Position(51.507780365645885, -0.21786745637655258),
            new Position(51.50802478194237, -0.21474729292094707),
            new Position(51.5086863655597, -0.20895397290587425))));
        getDs().save(london);

        Route londonToParis = new Route("London To Paris", new LineString(of(
            new Position(51.5286416, -0.1015987),
            new Position(48.858859, 2.3470599))));
        getDs().save(londonToParis);
        getDs().ensureIndexes();

        // when
        MongoCursor<Route> route = getDs().find(Route.class)
                                          .field("route")
                                          .intersects(new LineString(of(
                                              new Position(37.4043709, -5.9643244),
                                              new Position(37.4045286, -5.9642332))))
                                          .execute();

        // then
        assertThat(route.next(), is(sevilla));
        assertFalse(route.hasNext());
    }

}