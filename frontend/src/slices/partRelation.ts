import type { PayloadAction } from '@reduxjs/toolkit';
import { createSlice } from '@reduxjs/toolkit';
import type { AppThunk } from 'src/store';
import { PartRelation, PartRelationType } from '../models/owns/part';
import api from '../utils/api';
import { revertAll } from 'src/utils/redux';

interface PartRelationState {
  relationsByPart: { [id: number]: PartRelation[] };
  loadingRelations: { [id: number]: boolean };
}

const initialState: PartRelationState = {
  relationsByPart: {},
  loadingRelations: {}
};

const slice = createSlice({
  name: 'partRelations',
  initialState,
  extraReducers: (builder) => builder.addCase(revertAll, () => initialState),
  reducers: {
    getPartRelations(
      state: PartRelationState,
      action: PayloadAction<{ id: number; relations: PartRelation[] }>
    ) {
      const { relations, id } = action.payload;
      state.relationsByPart[id] = relations;
    },
    createPartRelation(
      state: PartRelationState,
      action: PayloadAction<{ partId: number; relation: PartRelation }>
    ) {
      const { relation, partId } = action.payload;
      if (state.relationsByPart[partId]) {
        state.relationsByPart[partId].push(relation);
      } else state.relationsByPart[partId] = [relation];
    },
    deletePartRelation(
      state: PartRelationState,
      action: PayloadAction<{ partId: number; id: number }>
    ) {
      const { id, partId } = action.payload;
      state.relationsByPart[partId] = (
        state.relationsByPart[partId] ?? []
      ).filter((relation) => relation.id !== id);
    },
    setLoadingByPart(
      state: PartRelationState,
      action: PayloadAction<{ loading: boolean; id: number }>
    ) {
      const { loading, id } = action.payload;
      state.loadingRelations = { ...state.loadingRelations, [id]: loading };
    }
  }
});

export const reducer = slice.reducer;

export const getPartRelations =
  (id: number): AppThunk =>
  async (dispatch) => {
    dispatch(slice.actions.setLoadingByPart({ id, loading: true }));
    try {
      const relations = await api.get<PartRelation[]>(`parts/${id}/relations`);
      dispatch(slice.actions.getPartRelations({ id, relations }));
    } catch (err) {
      console.error('Failed to load part relations:', err);
    } finally {
      dispatch(slice.actions.setLoadingByPart({ id, loading: false }));
    }
  };

export const createPartRelation =
  (
    partId: number,
    relation: { targetPart: { id: number }; relationType: PartRelationType }
  ): AppThunk =>
  async (dispatch) => {
    const relationResponse = await api.post<PartRelation>(
      `parts/${partId}/relations`,
      relation
    );
    dispatch(
      slice.actions.createPartRelation({ partId, relation: relationResponse })
    );
  };

export const deletePartRelation =
  (partId: number, id: number): AppThunk =>
  async (dispatch) => {
    const response = await api.deletes<{ success: boolean }>(
      `parts/${partId}/relations/${id}`
    );
    const { success } = response;
    if (success) {
      dispatch(slice.actions.deletePartRelation({ partId, id }));
    }
  };

export default slice;
