import {
  Autocomplete,
  Button,
  CircularProgress,
  Dialog,
  DialogContent,
  DialogTitle,
  Grid,
  TextField,
  Typography
} from '@mui/material';
import { useContext, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Formik } from 'formik';
import * as Yup from 'yup';
import { useDispatch, useSelector } from '../../../store';
import { createPartRelation } from '../../../slices/partRelation';
import { getPartsMini } from '../../../slices/part';
import { PartMiniDTO, PartRelationType } from '../../../models/owns/part';
import { CustomSnackBarContext } from '../../../contexts/CustomSnackBarContext';
import { getErrorMessage } from '../../../utils/api';

interface AddPartRelationModalProps {
  open: boolean;
  onClose: () => void;
  partId: number;
  relationType: PartRelationType;
  excludePartIds: number[];
}

export default function AddPartRelationModal({
  open,
  onClose,
  partId,
  relationType,
  excludePartIds
}: AddPartRelationModalProps) {
  const { t }: { t: any } = useTranslation();
  const dispatch = useDispatch();
  const { partsMini } = useSelector((state) => state.parts);
  const { showSnackBar } = useContext(CustomSnackBarContext);

  useEffect(() => {
    if (open) dispatch(getPartsMini());
  }, [open]);

  const options = partsMini.filter(
    (part) => part.id !== partId && !excludePartIds.includes(part.id)
  );

  return (
    <Dialog fullWidth maxWidth="sm" open={open} onClose={onClose}>
      <DialogTitle sx={{ p: 3 }}>
        <Typography variant="h4" gutterBottom>
          {relationType === 'SUBSTITUTE'
            ? t('add_substitute_part')
            : t('add_related_part')}
        </Typography>
      </DialogTitle>
      <Formik
        initialValues={{ targetPart: null as PartMiniDTO | null }}
        validationSchema={Yup.object().shape({
          targetPart: Yup.object().required(t('required_field')).nullable()
        })}
        onSubmit={(_values, { setSubmitting }) => {
          if (!_values.targetPart) return;
          setSubmitting(true);
          dispatch(
            createPartRelation(partId, {
              targetPart: { id: _values.targetPart.id },
              relationType
            })
          )
            .catch((err) => showSnackBar(getErrorMessage(err), 'error'))
            .finally(() => {
              setSubmitting(false);
              onClose();
            });
        }}
      >
        {({ errors, handleSubmit, setFieldValue, isSubmitting, values }) => (
          <form onSubmit={handleSubmit}>
            <DialogContent dividers sx={{ p: 3 }}>
              <Grid container spacing={3}>
                <Grid item xs={12}>
                  <Autocomplete
                    fullWidth
                    options={options}
                    getOptionLabel={(option) => option.name}
                    isOptionEqualToValue={(option, value) =>
                      option.id === value.id
                    }
                    onChange={(event, value) =>
                      setFieldValue('targetPart', value)
                    }
                    value={values.targetPart}
                    renderInput={(params) => (
                      <TextField
                        {...params}
                        fullWidth
                        variant="outlined"
                        placeholder={t('select')}
                        error={Boolean(errors.targetPart)}
                        helperText={
                          errors.targetPart ? t('required_field') : ''
                        }
                      />
                    )}
                  />
                </Grid>
                <Grid item xs={12}>
                  <Button
                    variant="contained"
                    type="submit"
                    startIcon={
                      isSubmitting ? <CircularProgress size="1rem" /> : null
                    }
                    disabled={isSubmitting}
                  >
                    {t('add')}
                  </Button>
                </Grid>
              </Grid>
            </DialogContent>
          </form>
        )}
      </Formik>
    </Dialog>
  );
}
