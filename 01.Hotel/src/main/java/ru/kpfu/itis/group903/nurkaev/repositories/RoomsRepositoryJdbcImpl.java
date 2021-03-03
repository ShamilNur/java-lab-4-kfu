package ru.kpfu.itis.group903.nurkaev.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.kpfu.itis.group903.nurkaev.exceptions.NoRoomsAvailableException;
import ru.kpfu.itis.group903.nurkaev.dto.AvailabilityDto;
import ru.kpfu.itis.group903.nurkaev.models.Room;

import javax.sql.DataSource;
import java.util.*;

import static ru.kpfu.itis.group903.nurkaev.queries.RoomQueries.*;

/**
 * @author Shamil Nurkaev @nshamil
 * 11-903
 * Sem 1
 */

@Repository(value = "roomsRepository")
public class RoomsRepositoryJdbcImpl implements RoomsRepository {
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final RowMapper<Room> roomRowMapper = (row, rowNumber) -> Room.builder()
            .id(row.getLong("id"))
            .name(row.getString("name"))
            .photo(row.getString("photo"))
            .dateFrom(row.getLong("date_from"))
            .dateTo(row.getLong("date_to"))
            .roomsNumber(row.getInt("rooms_number"))
            .adultsNumber(row.getInt("adults_number"))
            .childNumber(row.getInt("child_number"))
            .price(row.getInt("price"))
            .build();

    @Autowired
    public RoomsRepositoryJdbcImpl(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void save(Room entity) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", entity.getName());
        params.put("photo", entity.getPhoto());
        params.put("date_from", entity.getDateFrom());
        params.put("date_to", entity.getDateTo());
        params.put("rooms_number", entity.getRoomsNumber());
        params.put("adults_numder", entity.getAdultsNumber());
        params.put("child_numder", entity.getChildNumber());
        params.put("price", entity.getPrice());
        // Saving the entity and setting the id value generated by the database
        SqlParameterSource sqlParameterSource = new MapSqlParameterSource(params);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        namedParameterJdbcTemplate.update(SQL_INSERT, sqlParameterSource, keyHolder);
        Long id = Long.parseLong(Objects.requireNonNull(keyHolder.getKeys(),
                "The key was not generated, error with the database has occurred.")
                .get("id").toString());
        entity.setId(id);
    }

    @Override
    public void update(Room entity) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", entity.getName());
        params.put("date_from", entity.getDateFrom());
        params.put("date_to", entity.getDateTo());
        params.put("rooms_number", entity.getRoomsNumber());
        params.put("adults_number", entity.getAdultsNumber());
        params.put("child_number", entity.getChildNumber());
        namedParameterJdbcTemplate.update(SQL_UPDATE_BY_ID, params);
    }

    @Override
    public void delete(Room entity) {
        namedParameterJdbcTemplate.update(SQL_DELETE_BY_ID,
                Collections.singletonMap("id", entity.getId()));
    }

    @Override
    public Optional<Room> findById(Long id) {
        Room room;
        try {
            room = namedParameterJdbcTemplate.queryForObject(SQL_SELECT_BY_ID,
                    Collections.singletonMap("id", id), roomRowMapper);
        } catch (EmptyResultDataAccessException e) {
            room = null;
        }

        return Optional.ofNullable(room);
    }

    @Override
    public List<Room> findAll() {
        return namedParameterJdbcTemplate.query(SQL_SELECT, roomRowMapper);
    }

    @Override
    public Optional<Room> findByName(String name) {
        Room room;
        try {
            room = namedParameterJdbcTemplate.queryForObject(SQL_SELECT_BY_ID,
                    Collections.singletonMap("name", name), roomRowMapper);
        } catch (EmptyResultDataAccessException e) {
            room = null;
        }

        return Optional.ofNullable(room);

    }

    @Override
    public List<Room> getAvailableRooms(AvailabilityDto availabilityDto) throws NoRoomsAvailableException {
        List<Room> rooms = namedParameterJdbcTemplate.query(SQL_SELECT_BY_AVAILABLE_FORM,
                Collections.singletonMap("date_from", availabilityDto.getDateFrom()), roomRowMapper);
        if (rooms.size() > 0) {
            return rooms;
        } else throw new NoRoomsAvailableException();
    }
}
